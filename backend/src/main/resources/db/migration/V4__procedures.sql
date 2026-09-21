-- =============================================================================
-- Solidari — procedures PL/pgSQL
--
-- Nenhuma procedure faz COMMIT interno, por decisao de projeto: o banco de
-- desenvolvimento e o mesmo usado pelos testes, que rodam em @Transactional e
-- dependem de poder desfazer tudo ao final. COMMIT dentro de uma procedure
-- chamada com transacao aberta levantaria 'invalid transaction termination'.
--
-- Os SQLSTATE proprios (SL001, SL002...) permitem que o Java distinga regra de
-- negocio de falha tecnica sem depender do texto da mensagem.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- sp_registrar_doacao — acionada pelo back-end (REST -> Java -> JDBC -> banco)
--
-- Concentra a regra da doacao numa unica operacao atomica: valida saldo,
-- debita a carteira e grava a doacao. Antes isso eram tres passos soltos no
-- DoacaoService, com uma janela entre ler o saldo e grava-lo.
--
-- Parametros: p_usuario_id  IN  — quem doa
--             p_parceiro_id IN  — parceiro vinculado (opcional, pode ser NULL)
--             p_valor       IN  — valor da doacao
--             p_categoria   IN  — RESTAURACAO_AMBIENTAL ou BEM_ESTAR_COMUNITARIO
--             p_doacao_id   OUT — id da doacao criada
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_registrar_doacao(
    p_usuario_id  IN  BIGINT,
    p_parceiro_id IN  BIGINT,
    p_valor       IN  NUMERIC,
    p_categoria   IN  VARCHAR,
    p_doacao_id   OUT BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_saldo NUMERIC;
BEGIN
    IF p_valor IS NULL OR p_valor <= 0 THEN
        RAISE EXCEPTION 'Valor da doação deve ser maior que zero'
            USING ERRCODE = 'SL003';
    END IF;

    IF p_categoria NOT IN ('RESTAURACAO_AMBIENTAL', 'BEM_ESTAR_COMUNITARIO') THEN
        RAISE EXCEPTION 'Categoria de impacto inválida: %', p_categoria
            USING ERRCODE = 'SL004';
    END IF;

    -- FOR UPDATE serializa doacoes concorrentes do mesmo usuario: sem isso duas
    -- requisicoes simultaneas poderiam ler o mesmo saldo e gastar duas vezes.
    SELECT saldo INTO v_saldo
    FROM usuarios
    WHERE id = p_usuario_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Usuário % não encontrado', p_usuario_id
            USING ERRCODE = 'SL002';
    END IF;

    IF v_saldo < p_valor THEN
        RAISE EXCEPTION 'Saldo insuficiente: disponível R$ %, solicitado R$ %',
                        fn_moeda_br(v_saldo), fn_moeda_br(p_valor)
            USING ERRCODE = 'SL001';
    END IF;

    IF p_parceiro_id IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM parceiros WHERE id = p_parceiro_id) THEN
        RAISE EXCEPTION 'Parceiro % não encontrado', p_parceiro_id
            USING ERRCODE = 'SL002';
    END IF;

    UPDATE usuarios
    SET saldo = saldo - p_valor
    WHERE id = p_usuario_id;

    INSERT INTO doacoes (usuario_id, parceiro_id, valor, categoria_impacto, data)
    VALUES (p_usuario_id, p_parceiro_id, p_valor, p_categoria, now())
    RETURNING id INTO p_doacao_id;
END;
$$;

COMMENT ON PROCEDURE sp_registrar_doacao(BIGINT, BIGINT, NUMERIC, VARCHAR, BIGINT) IS
    'Registra uma doacao de forma atomica: valida saldo, debita a carteira e grava';


-- -----------------------------------------------------------------------------
-- sp_processar_cashback — rotina automatizada em lote
--
-- Varre as transacoes PENDENTE com CURSOR, apura o credito de cada uma pelo
-- percentual do parceiro e soma ao saldo do usuario. E o que faz o saldo
-- existir: sem esta rotina nenhuma carteira e alimentada.
--
-- Cada item roda em bloco proprio com EXCEPTION, entao uma transacao com
-- problema e marcada como ERRO sem derrubar o lote inteiro.
--
-- Parametros: p_limite          IN  — teto de transacoes por execucao (sem DEFAULT:
--                                   no Postgres, parametro OUT nao pode vir depois
--                                   de um parametro com valor padrao)
--             p_processadas     OUT — quantas foram creditadas
--             p_com_erro        OUT — quantas falharam
--             p_total_creditado OUT — soma creditada nas carteiras
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_processar_cashback(
    p_limite          IN  INT,
    p_processadas     OUT INT,
    p_com_erro        OUT INT,
    p_total_creditado OUT NUMERIC
)
LANGUAGE plpgsql
AS $$
DECLARE
    c_pendentes CURSOR FOR
        SELECT t.id,
               t.usuario_id,
               t.valor,
               p.percentual_cashback,
               p.ativo
        FROM transacoes t
        JOIN parceiros p ON p.id = t.parceiro_id
        WHERE t.status = 'PENDENTE'
        ORDER BY t.data
        LIMIT p_limite
        FOR UPDATE OF t;

    v_item     RECORD;
    v_cashback NUMERIC;
BEGIN
    p_processadas     := 0;
    p_com_erro        := 0;
    p_total_creditado := 0;

    OPEN c_pendentes;
    LOOP
        FETCH c_pendentes INTO v_item;
        EXIT WHEN NOT FOUND;

        BEGIN
            IF NOT v_item.ativo THEN
                RAISE EXCEPTION 'Parceiro inativo no momento do processamento'
                    USING ERRCODE = 'SL005';
            END IF;

            v_cashback := ROUND(v_item.valor * v_item.percentual_cashback / 100, 2);

            IF v_cashback <= 0 THEN
                -- Parceiro sem cashback: nao e erro, so nao gera credito.
                UPDATE transacoes
                SET status = 'PROCESSADA', cashback_gerado = 0, processada_em = now()
                WHERE id = v_item.id;

                p_processadas := p_processadas + 1;
                CONTINUE;
            END IF;

            UPDATE usuarios
            SET saldo = saldo + v_cashback
            WHERE id = v_item.usuario_id;

            UPDATE transacoes
            SET status = 'PROCESSADA', cashback_gerado = v_cashback,
                processada_em = now(), erro = NULL
            WHERE id = v_item.id;

            p_processadas     := p_processadas + 1;
            p_total_creditado := p_total_creditado + v_cashback;

        EXCEPTION
            WHEN OTHERS THEN
                -- O bloco aninhado desfaz so este item; o lote continua.
                UPDATE transacoes
                SET status = 'ERRO',
                    erro = left(SQLERRM, 400),
                    processada_em = now()
                WHERE id = v_item.id;

                p_com_erro := p_com_erro + 1;
        END;
    END LOOP;
    CLOSE c_pendentes;

    RAISE NOTICE 'Cashback processado: % creditadas, % com erro, total R$ %',
                 p_processadas, p_com_erro, fn_moeda_br(p_total_creditado);
END;
$$;

COMMENT ON PROCEDURE sp_processar_cashback(INT, INT, INT, NUMERIC) IS
    'Processa em lote as transacoes pendentes e credita o cashback na carteira do usuario';


-- -----------------------------------------------------------------------------
-- sp_gerar_alertas — rotina de monitoramento
--
-- Equivalente, no dominio do Solidari, ao alerta por leitura critica de sensor:
-- varre o estado do sistema e registra o que precisa de atencao humana.
--
-- Regras avaliadas:
--   CRITICO  credito parado: saldo relevante sem nenhuma doacao ha mais de 60 dias
--   ATENCAO  parceiro sem movimento: nenhuma transacao nos ultimos 45 dias
--   ATENCAO  transacoes em ERRO aguardando reprocessamento
--
-- Parametros: p_gerados OUT — quantos alertas foram criados nesta execucao
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_gerar_alertas(p_gerados OUT INT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_registro    RECORD;
    v_dias        INT;
    v_com_erro    INT;
BEGIN
    p_gerados := 0;

    -- 1) Credito parado na carteira
    FOR v_registro IN
        SELECT u.id, u.nome, u.saldo,
               (SELECT MAX(d.data) FROM doacoes d WHERE d.usuario_id = u.id) AS ultima
        FROM usuarios u
        WHERE u.papel = 'USUARIO' AND u.saldo >= 50
    LOOP
        v_dias := COALESCE(EXTRACT(DAY FROM now() - v_registro.ultima)::INT, 9999);

        IF v_dias > 60 THEN
            INSERT INTO alertas (tipo, severidade, descricao, usuario_id)
            VALUES (
                'CREDITO_PARADO',
                'CRITICO',
                format('%s tem R$ %s disponíveis e não doa há %s dias',
                       v_registro.nome, fn_moeda_br(v_registro.saldo),
                       CASE WHEN v_dias = 9999 THEN 'sempre' ELSE v_dias::TEXT END),
                v_registro.id
            );
            p_gerados := p_gerados + 1;
        END IF;
    END LOOP;

    -- 2) Parceiro sem movimento recente
    FOR v_registro IN
        SELECT p.id, p.nome,
               (SELECT MAX(t.data) FROM transacoes t WHERE t.parceiro_id = p.id) AS ultima
        FROM parceiros p
        WHERE p.ativo
    LOOP
        IF v_registro.ultima IS NULL OR v_registro.ultima < now() - INTERVAL '45 days' THEN
            INSERT INTO alertas (tipo, severidade, descricao, parceiro_id)
            VALUES (
                'PARCEIRO_SEM_MOVIMENTO',
                'ATENCAO',
                format('Parceiro %s sem transações desde %s',
                       v_registro.nome,
                       COALESCE(to_char(v_registro.ultima, 'DD/MM/YYYY'), 'o cadastro')),
                v_registro.id
            );
            p_gerados := p_gerados + 1;
        END IF;
    END LOOP;

    -- 3) Transacoes com erro pendentes de reprocessamento
    SELECT COUNT(*) INTO v_com_erro FROM transacoes WHERE status = 'ERRO';

    IF v_com_erro > 0 THEN
        INSERT INTO alertas (tipo, severidade, descricao)
        VALUES ('TRANSACOES_COM_ERRO', 'ATENCAO',
                format('%s transação(ões) em ERRO aguardando reprocessamento', v_com_erro));
        p_gerados := p_gerados + 1;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Falha ao gerar alertas: %', SQLERRM
            USING ERRCODE = 'SL006';
END;
$$;

COMMENT ON PROCEDURE sp_gerar_alertas(INT) IS
    'Varre o sistema e registra alertas operacionais na tabela alertas';
