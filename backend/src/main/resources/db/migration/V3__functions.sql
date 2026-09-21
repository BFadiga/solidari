-- =============================================================================
-- Solidari — functions PL/pgSQL
--
-- Regras que antes viviam espalhadas no Java passam a morar no banco, perto dos
-- dados. Duas consequencias praticas: o calculo fica disponivel para qualquer
-- consumidor (API, dashboard, consulta manual) e deixa de ser reimplementado a
-- cada cliente novo.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- fn_moeda_br — formatacao monetaria auxiliar
--
-- to_char com 'D' e 'G' usa o locale do servidor: num banco en_US sai 1,234.56.
-- Os literais '.' e ',' na mascara, ao contrario, sao fixos — entao formatamos
-- no padrao americano e trocamos os separadores. O resultado nao depende do
-- locale da maquina onde o banco foi criado.
--
-- Parametros: p_valor IN
-- Retorno:    valor no padrao brasileiro, sem prefixo (ex.: 1.234,56)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_moeda_br(p_valor IN NUMERIC)
RETURNS TEXT
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
    IF p_valor IS NULL THEN
        RETURN '0,00';
    END IF;

    RETURN translate(to_char(p_valor, 'FM999,999,990.00'), '.,', ',.');
END;
$$;

COMMENT ON FUNCTION fn_moeda_br(NUMERIC) IS
    'Formata valor monetario no padrao brasileiro, independente do locale do servidor';


-- -----------------------------------------------------------------------------
-- fn_impacto_usuario — INDICADOR
--
-- Converte o total doado em impacto concreto, que e a promessa do produto:
-- restauracao ambiental vira arvores (R$ 10 cada), bem-estar comunitario vira
-- refeicoes (R$ 5 cada). Ate aqui esse calculo estava em Java, no DoacaoService,
-- com as constantes fixas no codigo.
--
-- Parametros: p_usuario_id IN — identificador do usuario
-- Retorno:    uma linha com arvores, refeicoes e total doado
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_impacto_usuario(p_usuario_id IN BIGINT)
RETURNS TABLE (
    arvores_plantadas  BIGINT,
    refeicoes_servidas BIGINT,
    total_doado        NUMERIC
)
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_valor_arvore   CONSTANT NUMERIC := 10.00;
    v_valor_refeicao CONSTANT NUMERIC := 5.00;
    v_ambiental      NUMERIC := 0;
    v_comunitario    NUMERIC := 0;
    v_existe         BOOLEAN;
BEGIN
    -- Usuario inexistente e erro de chamada, nao "impacto zero": sem esta
    -- checagem um id errado devolveria 0/0/0 silenciosamente.
    SELECT EXISTS (SELECT 1 FROM usuarios WHERE id = p_usuario_id) INTO v_existe;

    IF NOT v_existe THEN
        RAISE EXCEPTION 'Usuário % não encontrado', p_usuario_id
            USING ERRCODE = 'no_data_found';
    END IF;

    SELECT
        COALESCE(SUM(d.valor) FILTER (WHERE d.categoria_impacto = 'RESTAURACAO_AMBIENTAL'), 0),
        COALESCE(SUM(d.valor) FILTER (WHERE d.categoria_impacto = 'BEM_ESTAR_COMUNITARIO'), 0)
    INTO v_ambiental, v_comunitario
    FROM doacoes d
    WHERE d.usuario_id = p_usuario_id;

    RETURN QUERY SELECT
        FLOOR(v_ambiental   / v_valor_arvore)::BIGINT,
        FLOOR(v_comunitario / v_valor_refeicao)::BIGINT,
        ROUND(v_ambiental + v_comunitario, 2);

EXCEPTION
    WHEN division_by_zero THEN
        -- Protege contra alteracao futura das constantes para zero.
        RAISE EXCEPTION 'Constante de conversao de impacto invalida (divisao por zero)';
END;
$$;

COMMENT ON FUNCTION fn_impacto_usuario(BIGINT) IS
    'Indicador de impacto do usuario: arvores plantadas, refeicoes servidas e total doado';


-- -----------------------------------------------------------------------------
-- fn_extrato_formatado — DADOS FORMATADOS
--
-- Monta o extrato de doacoes ja pronto para exibicao, em portugues: valores em
-- R$ 1.234,56 e datas em DD/MM/AAAA. Percorre as doacoes com CURSOR explicito.
--
-- Parametros: p_usuario_id IN — identificador do usuario
--             p_limite     IN — quantas doacoes listar (padrao 10)
-- Retorno:    bloco de texto pronto para a tela de extrato
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_extrato_formatado(
    p_usuario_id IN BIGINT,
    p_limite     IN INT DEFAULT 10
)
RETURNS TEXT
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    c_doacoes CURSOR FOR
        SELECT d.data,
               d.valor,
               d.categoria_impacto,
               COALESCE(p.nome, 'Doação direta') AS parceiro
        FROM doacoes d
        LEFT JOIN parceiros p ON p.id = d.parceiro_id
        WHERE d.usuario_id = p_usuario_id
        ORDER BY d.data DESC
        LIMIT p_limite;

    v_linha     RECORD;
    v_saida     TEXT := '';
    v_nome      TEXT;
    v_saldo     NUMERIC;
    v_total     NUMERIC := 0;
    v_contador  INT := 0;
BEGIN
    SELECT nome, saldo INTO STRICT v_nome, v_saldo
    FROM usuarios WHERE id = p_usuario_id;

    v_saida := format('EXTRATO SOLIDARI — %s', v_nome) || chr(10)
            || repeat('-', 58) || chr(10);

    OPEN c_doacoes;
    LOOP
        FETCH c_doacoes INTO v_linha;
        EXIT WHEN NOT FOUND;

        v_contador := v_contador + 1;
        v_total    := v_total + v_linha.valor;

        v_saida := v_saida || format(
            '%s  %-22s  %14s  %s',
            to_char(v_linha.data, 'DD/MM/YYYY'),
            substr(v_linha.parceiro, 1, 22),
            'R$ ' || fn_moeda_br(v_linha.valor),
            CASE v_linha.categoria_impacto
                WHEN 'RESTAURACAO_AMBIENTAL' THEN 'Ambiental'
                ELSE 'Comunitário'
            END
        ) || chr(10);
    END LOOP;
    CLOSE c_doacoes;

    IF v_contador = 0 THEN
        RETURN v_saida || 'Nenhuma doação registrada até o momento.' || chr(10);
    END IF;

    v_saida := v_saida
            || repeat('-', 58) || chr(10)
            || format('%s doação(ões)   Total: R$ %s   Saldo atual: R$ %s',
                      v_contador,
                      fn_moeda_br(v_total),
                      fn_moeda_br(v_saldo))
            || chr(10);

    RETURN v_saida;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- SELECT INTO STRICT sem linha: id inexistente.
        RETURN format('Usuário %s não encontrado.', p_usuario_id);
    WHEN OTHERS THEN
        -- O cursor pode ter ficado aberto se o erro ocorreu dentro do LOOP.
        IF c_doacoes%ISOPEN THEN
            CLOSE c_doacoes;
        END IF;
        RAISE;
END;
$$;

COMMENT ON FUNCTION fn_extrato_formatado(BIGINT, INT) IS
    'Extrato de doacoes formatado em pt-BR (R$ e DD/MM/AAAA), percorrido por cursor';


-- -----------------------------------------------------------------------------
-- fn_ranking_doador — INDICADOR DE POSICAO
--
-- Posicao do usuario no ranking de total doado. Feita para ser usada dentro de
-- consultas SQL comuns, e nao so chamada isoladamente.
--
-- Parametros: p_usuario_id IN
-- Retorno:    posicao (1 = maior doador); 0 se o usuario ainda nao doou
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_ranking_doador(p_usuario_id IN BIGINT)
RETURNS INT
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_posicao INT;
BEGIN
    SELECT r.posicao INTO v_posicao
    FROM (
        SELECT usuario_id,
               RANK() OVER (ORDER BY SUM(valor) DESC) AS posicao
        FROM doacoes
        GROUP BY usuario_id
    ) r
    WHERE r.usuario_id = p_usuario_id;

    -- Usuario sem doacao nao aparece no agrupamento; 0 significa "fora do ranking".
    RETURN COALESCE(v_posicao, 0);
END;
$$;

COMMENT ON FUNCTION fn_ranking_doador(BIGINT) IS
    'Posicao do usuario no ranking por total doado; 0 quando ainda nao doou';
