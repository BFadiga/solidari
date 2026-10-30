-- =============================================================================
-- Rotinas operacionais em lote: apuracao de cashback e monitoramento
--
--   pkg_cashback       apura o credito das compras pendentes
--   pkg_monitoramento  varre o sistema e registra alertas para o painel admin
--
-- Ficam em packages separados porque mudam por motivos diferentes: regra de
-- credito (financeiro) x regra de alerta (operacao). Quem recebe permissao de
-- EXECUTE em um nao ganha acesso ao outro.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- pkg_cashback
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PACKAGE pkg_cashback AS

    -- Teto padrao de transacoes por execucao (variavel global do package).
    c_limite_padrao CONSTANT PLS_INTEGER := 500;

    -- Processa em lote as transacoes PENDENTE e credita o cashback na carteira.
    PROCEDURE sp_processar_cashback(
        p_limite           IN  PLS_INTEGER,
        p_processadas      OUT PLS_INTEGER,
        p_com_erro         OUT PLS_INTEGER,
        p_total_creditado  OUT NUMBER
    );

END pkg_cashback;
/

CREATE OR REPLACE PACKAGE BODY pkg_cashback AS

    -- -------------------------------------------------------------------------
    -- sp_processar_cashback
    --
    -- Cada item roda entre um SAVEPOINT e seu bloco EXCEPTION. Diferente do
    -- PostgreSQL, no Oracle um erro desfaz so o comando que falhou, e nao o
    -- bloco inteiro: o ROLLBACK TO SAVEPOINT e o que garante que um item com
    -- problema nao deixe credito pela metade. O lote continua com os demais.
    --
    -- A troca de status usa "WHERE status = 'PENDENTE'": se outra execucao ja
    -- processou a transacao, SQL%ROWCOUNT volta 0 e o credito nao e duplicado.
    -- -------------------------------------------------------------------------
    PROCEDURE sp_processar_cashback(
        p_limite           IN  PLS_INTEGER,
        p_processadas      OUT PLS_INTEGER,
        p_com_erro         OUT PLS_INTEGER,
        p_total_creditado  OUT NUMBER
    ) IS
        CURSOR c_pendentes (cp_limite PLS_INTEGER) IS
            SELECT t.id,
                   t.usuario_id,
                   t.valor,
                   p.percentual_cashback,
                   p.ativo
              FROM transacoes t
              JOIN parceiros p ON p.id = t.parceiro_id
             WHERE t.status = 'PENDENTE'
             ORDER BY t.data, t.id
             FETCH FIRST cp_limite ROWS ONLY;

        v_item      c_pendentes%ROWTYPE;
        v_cashback  NUMBER;
        v_erro      VARCHAR2(400);
    BEGIN
        p_processadas     := 0;
        p_com_erro        := 0;
        p_total_creditado := 0;

        OPEN c_pendentes(NVL(p_limite, c_limite_padrao));
        LOOP
            FETCH c_pendentes INTO v_item;
            EXIT WHEN c_pendentes%NOTFOUND;

            SAVEPOINT sp_item_cashback;
            BEGIN
                IF v_item.ativo = 0 THEN
                    pkg_util.sp_erro(pkg_util.c_erro_parceiro_inativo,
                                     'Parceiro inativo no momento do processamento');
                END IF;

                v_cashback := ROUND(v_item.valor * v_item.percentual_cashback / 100, 2);

                -- processada_em e preenchida pela trigger trg_transacao_status.
                UPDATE transacoes
                   SET status          = 'PROCESSADA',
                       cashback_gerado = v_cashback,
                       erro            = NULL
                 WHERE id = v_item.id
                   AND status = 'PENDENTE';

                IF SQL%ROWCOUNT = 1 THEN
                    -- Parceiro sem cashback nao e erro: so nao gera credito.
                    IF v_cashback > 0 THEN
                        UPDATE usuarios
                           SET saldo = saldo + v_cashback
                         WHERE id = v_item.usuario_id;

                        p_total_creditado := p_total_creditado + v_cashback;
                    END IF;

                    p_processadas := p_processadas + 1;
                END IF;

            EXCEPTION
                WHEN OTHERS THEN
                    v_erro := SUBSTR(SQLERRM, 1, 400);
                    ROLLBACK TO SAVEPOINT sp_item_cashback;

                    -- A mudanca para ERRO dispara trg_alerta_transacao_erro.
                    UPDATE transacoes
                       SET status = 'ERRO',
                           erro   = v_erro
                     WHERE id = v_item.id;

                    p_com_erro := p_com_erro + 1;
            END;
        END LOOP;
        CLOSE c_pendentes;

        DBMS_OUTPUT.PUT_LINE('Cashback processado: ' || p_processadas || ' creditadas, '
                             || p_com_erro || ' com erro, total R$ '
                             || pkg_util.fn_moeda_br(p_total_creditado));
    EXCEPTION
        WHEN OTHERS THEN
            IF c_pendentes%ISOPEN THEN
                CLOSE c_pendentes;
            END IF;
            RAISE;
    END sp_processar_cashback;

END pkg_cashback;
/

-- -----------------------------------------------------------------------------
-- pkg_monitoramento
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PACKAGE pkg_monitoramento AS

    -- Limiares das regras de alerta (variaveis globais do package).
    c_saldo_minimo_parado   CONSTANT NUMBER      := 50;   -- R$
    c_dias_credito_parado   CONSTANT PLS_INTEGER := 60;
    c_dias_parceiro_parado  CONSTANT PLS_INTEGER := 45;

    -- Varre o sistema e registra alertas operacionais; devolve quantos criou.
    PROCEDURE sp_gerar_alertas(p_gerados OUT PLS_INTEGER);

END pkg_monitoramento;
/

CREATE OR REPLACE PACKAGE BODY pkg_monitoramento AS

    -- -------------------------------------------------------------------------
    -- fn_alerta_aberto — PRIVADA (nao esta na especificacao)
    --
    -- Evita duplicar alerta: se ja existe um alerta igual ainda nao resolvido,
    -- rodar a rotina de novo nao cria outro.
    -- -------------------------------------------------------------------------
    FUNCTION fn_alerta_aberto(
        p_tipo        IN VARCHAR2,
        p_usuario_id  IN NUMBER,
        p_parceiro_id IN NUMBER
    ) RETURN BOOLEAN IS
        v_qtd PLS_INTEGER;
    BEGIN
        SELECT COUNT(*)
          INTO v_qtd
          FROM alertas
         WHERE tipo = p_tipo
           AND resolvido = 0
           AND (usuario_id  = p_usuario_id  OR (usuario_id  IS NULL AND p_usuario_id  IS NULL))
           AND (parceiro_id = p_parceiro_id OR (parceiro_id IS NULL AND p_parceiro_id IS NULL));

        RETURN v_qtd > 0;
    END fn_alerta_aberto;

    -- -------------------------------------------------------------------------
    -- sp_gerar_alertas
    --
    -- Regras avaliadas:
    --   CRITICO  credito parado: saldo relevante sem doacao ha mais de 60 dias
    --   ATENCAO  parceiro sem movimento: nenhuma transacao nos ultimos 45 dias
    --   ATENCAO  transacoes em ERRO aguardando reprocessamento
    -- -------------------------------------------------------------------------
    PROCEDURE sp_gerar_alertas(p_gerados OUT PLS_INTEGER) IS
        v_dias      PLS_INTEGER;
        v_com_erro  PLS_INTEGER;
    BEGIN
        p_gerados := 0;

        -- 1) Credito parado na carteira
        FOR r IN (SELECT u.id, u.nome, u.saldo,
                         (SELECT MAX(d.data) FROM doacoes d WHERE d.usuario_id = u.id) AS ultima
                    FROM usuarios u
                   WHERE u.papel = 'USUARIO'
                     AND u.saldo >= c_saldo_minimo_parado)
        LOOP
            v_dias := CASE
                          WHEN r.ultima IS NULL THEN 9999
                          ELSE EXTRACT(DAY FROM (LOCALTIMESTAMP - r.ultima))
                      END;

            IF v_dias > c_dias_credito_parado
               AND NOT fn_alerta_aberto('CREDITO_PARADO', r.id, NULL) THEN
                INSERT INTO alertas (tipo, severidade, descricao, usuario_id)
                VALUES ('CREDITO_PARADO',
                        'CRITICO',
                        r.nome || ' tem R$ ' || pkg_util.fn_moeda_br(r.saldo)
                          || ' disponíveis e não doa há '
                          || CASE WHEN v_dias = 9999 THEN 'sempre' ELSE v_dias || ' dias' END,
                        r.id);
                p_gerados := p_gerados + 1;
            END IF;
        END LOOP;

        -- 2) Parceiro sem movimento recente
        FOR r IN (SELECT p.id, p.nome,
                         (SELECT MAX(t.data) FROM transacoes t WHERE t.parceiro_id = p.id) AS ultima
                    FROM parceiros p
                   WHERE p.ativo = 1)
        LOOP
            IF (r.ultima IS NULL
                OR r.ultima < LOCALTIMESTAMP - NUMTODSINTERVAL(c_dias_parceiro_parado, 'DAY'))
               AND NOT fn_alerta_aberto('PARCEIRO_SEM_MOVIMENTO', NULL, r.id) THEN
                INSERT INTO alertas (tipo, severidade, descricao, parceiro_id)
                VALUES ('PARCEIRO_SEM_MOVIMENTO',
                        'ATENCAO',
                        'Parceiro ' || r.nome || ' sem transações desde '
                          || NVL(TO_CHAR(r.ultima, 'DD/MM/YYYY'), 'o cadastro'),
                        r.id);
                p_gerados := p_gerados + 1;
            END IF;
        END LOOP;

        -- 3) Transacoes com erro pendentes de reprocessamento
        SELECT COUNT(*) INTO v_com_erro FROM transacoes WHERE status = 'ERRO';

        IF v_com_erro > 0 AND NOT fn_alerta_aberto('TRANSACOES_COM_ERRO', NULL, NULL) THEN
            INSERT INTO alertas (tipo, severidade, descricao)
            VALUES ('TRANSACOES_COM_ERRO', 'ATENCAO',
                    v_com_erro || ' transação(ões) em ERRO aguardando reprocessamento');
            p_gerados := p_gerados + 1;
        END IF;

    EXCEPTION
        WHEN OTHERS THEN
            pkg_util.sp_erro(pkg_util.c_erro_rotina, 'Falha ao gerar alertas: ' || SQLERRM);
    END sp_gerar_alertas;

END pkg_monitoramento;
/
