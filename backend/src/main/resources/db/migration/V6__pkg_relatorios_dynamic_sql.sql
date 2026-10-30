-- =============================================================================
-- pkg_relatorios — consultas flexiveis para o painel administrativo (Dynamic SQL)
--
-- O painel precisa filtrar transacoes por qualquer combinacao de criterios
-- (status, parceiro, categoria, periodo, valor minimo) e agrupar o resumo por
-- dimensoes diferentes. Com SQL estatico seria preciso um SELECT para cada
-- combinacao, ou um WHERE cheio de "(:x IS NULL OR col = :x)" que o otimizador
-- nao consegue aproveitar bem. Aqui o comando e MONTADO em tempo de execucao
-- so com os filtros informados e aberto com OPEN ... FOR, devolvendo um
-- SYS_REFCURSOR que o Java le como ResultSet.
--
-- Seguranca (nada do usuario e concatenado no SQL):
--   * Valores de filtro entram sempre como BIND VARIABLES (:nome ... USING).
--   * Partes que nao aceitam bind (coluna do ORDER BY, expressao do GROUP BY)
--     vem de uma LISTA FECHADA dentro do package; valor fora da lista gera erro.
--
-- Tecnica de binds com quantidade fixa: OPEN ... FOR ... USING exige saber em
-- tempo de compilacao quantos binds existem. Filtro informado vira
-- "AND t.status = :status"; filtro ausente vira "AND :status IS NULL", que e
-- sempre verdadeiro quando o bind e nulo. Assim a lista USING e sempre a mesma.
-- =============================================================================

CREATE OR REPLACE PACKAGE pkg_relatorios AS

    c_limite_maximo CONSTANT PLS_INTEGER := 1000;

    -- Busca de transacoes com filtros opcionais e ordenacao escolhida.
    --   p_ordenar_por: DATA (padrao), VALOR, CASHBACK, PARCEIRO, USUARIO
    -- Colunas do cursor: id, usuario, parceiro, categoria, valor,
    --                    cashback_gerado, status, data
    FUNCTION fn_buscar_transacoes(
        p_status        IN VARCHAR2    DEFAULT NULL,
        p_parceiro_id   IN NUMBER      DEFAULT NULL,
        p_categoria     IN VARCHAR2    DEFAULT NULL,
        p_data_inicio   IN DATE        DEFAULT NULL,
        p_data_fim      IN DATE        DEFAULT NULL,
        p_valor_minimo  IN NUMBER      DEFAULT NULL,
        p_ordenar_por   IN VARCHAR2    DEFAULT 'DATA',
        p_limite        IN PLS_INTEGER DEFAULT 100
    ) RETURN SYS_REFCURSOR;

    -- Resumo agregado das transacoes por uma dimensao escolhida.
    --   p_dimensao: PARCEIRO, CATEGORIA, STATUS ou MES
    -- Colunas do cursor: chave, quantidade, total_valor, total_cashback
    FUNCTION fn_resumo_transacoes(
        p_dimensao  IN VARCHAR2,
        p_status    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR;

    -- Monta o texto do SQL da busca sem executar (usado na documentacao e nos
    -- testes para mostrar o comando gerado).
    FUNCTION fn_sql_busca(
        p_status        IN VARCHAR2 DEFAULT NULL,
        p_parceiro_id   IN NUMBER   DEFAULT NULL,
        p_categoria     IN VARCHAR2 DEFAULT NULL,
        p_data_inicio   IN DATE     DEFAULT NULL,
        p_data_fim      IN DATE     DEFAULT NULL,
        p_valor_minimo  IN NUMBER   DEFAULT NULL,
        p_ordenar_por   IN VARCHAR2 DEFAULT 'DATA'
    ) RETURN VARCHAR2;

END pkg_relatorios;
/

CREATE OR REPLACE PACKAGE BODY pkg_relatorios AS

    -- -------------------------------------------------------------------------
    -- Rotinas privadas de validacao (lista fechada)
    -- -------------------------------------------------------------------------
    PROCEDURE sp_validar_status(p_status IN VARCHAR2) IS
    BEGIN
        IF p_status IS NOT NULL
           AND UPPER(p_status) NOT IN ('PENDENTE', 'PROCESSADA', 'ERRO') THEN
            pkg_util.sp_erro(pkg_util.c_erro_filtro_invalido,
                             'Status inválido: ' || p_status
                             || ' (use PENDENTE, PROCESSADA ou ERRO)');
        END IF;
    END sp_validar_status;

    FUNCTION fn_coluna_ordenacao(p_ordenar_por IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE UPPER(NVL(p_ordenar_por, 'DATA'))
                   WHEN 'DATA'     THEN 't.data DESC, t.id DESC'
                   WHEN 'VALOR'    THEN 't.valor DESC, t.id DESC'
                   WHEN 'CASHBACK' THEN 't.cashback_gerado DESC NULLS LAST, t.id DESC'
                   WHEN 'PARCEIRO' THEN 'p.nome, t.data DESC'
                   WHEN 'USUARIO'  THEN 'u.nome, t.data DESC'
                   ELSE NULL
               END;
    END fn_coluna_ordenacao;

    FUNCTION fn_expressao_dimensao(p_dimensao IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE UPPER(p_dimensao)
                   WHEN 'PARCEIRO'  THEN 'p.nome'
                   WHEN 'CATEGORIA' THEN 'p.categoria'
                   WHEN 'STATUS'    THEN 't.status'
                   WHEN 'MES'       THEN 'TO_CHAR(t.data, ''YYYY-MM'')'
                   ELSE NULL
               END;
    END fn_expressao_dimensao;

    -- -------------------------------------------------------------------------
    -- fn_sql_busca — monta o comando dinamico
    -- -------------------------------------------------------------------------
    FUNCTION fn_sql_busca(
        p_status        IN VARCHAR2 DEFAULT NULL,
        p_parceiro_id   IN NUMBER   DEFAULT NULL,
        p_categoria     IN VARCHAR2 DEFAULT NULL,
        p_data_inicio   IN DATE     DEFAULT NULL,
        p_data_fim      IN DATE     DEFAULT NULL,
        p_valor_minimo  IN NUMBER   DEFAULT NULL,
        p_ordenar_por   IN VARCHAR2 DEFAULT 'DATA'
    ) RETURN VARCHAR2 IS
        v_sql       VARCHAR2(4000);
        v_ordenacao VARCHAR2(100);
    BEGIN
        sp_validar_status(p_status);

        v_ordenacao := fn_coluna_ordenacao(p_ordenar_por);
        IF v_ordenacao IS NULL THEN
            pkg_util.sp_erro(pkg_util.c_erro_filtro_invalido,
                             'Ordenação inválida: ' || p_ordenar_por
                             || ' (use DATA, VALOR, CASHBACK, PARCEIRO ou USUARIO)');
        END IF;

        v_sql := 'SELECT t.id, u.nome AS usuario, p.nome AS parceiro, p.categoria, '
              || 't.valor, t.cashback_gerado, t.status, t.data '
              || 'FROM transacoes t '
              || 'JOIN usuarios u ON u.id = t.usuario_id '
              || 'JOIN parceiros p ON p.id = t.parceiro_id '
              || 'WHERE 1 = 1';

        -- Cada filtro: presente -> condicao real; ausente -> bind neutro.
        v_sql := v_sql || CASE WHEN p_status IS NOT NULL
                               THEN ' AND t.status = :status'
                               ELSE ' AND :status IS NULL' END;

        v_sql := v_sql || CASE WHEN p_parceiro_id IS NOT NULL
                               THEN ' AND t.parceiro_id = :parceiro_id'
                               ELSE ' AND :parceiro_id IS NULL' END;

        v_sql := v_sql || CASE WHEN p_categoria IS NOT NULL
                               THEN ' AND UPPER(p.categoria) = UPPER(:categoria)'
                               ELSE ' AND :categoria IS NULL' END;

        v_sql := v_sql || CASE WHEN p_data_inicio IS NOT NULL
                               THEN ' AND t.data >= :data_inicio'
                               ELSE ' AND :data_inicio IS NULL' END;

        -- Data final inclusiva: tudo antes do inicio do dia seguinte.
        v_sql := v_sql || CASE WHEN p_data_fim IS NOT NULL
                               THEN ' AND t.data < :data_fim + 1'
                               ELSE ' AND :data_fim IS NULL' END;

        v_sql := v_sql || CASE WHEN p_valor_minimo IS NOT NULL
                               THEN ' AND t.valor >= :valor_minimo'
                               ELSE ' AND :valor_minimo IS NULL' END;

        RETURN v_sql || ' ORDER BY ' || v_ordenacao || ' FETCH FIRST :limite ROWS ONLY';
    END fn_sql_busca;

    -- -------------------------------------------------------------------------
    -- fn_buscar_transacoes — OPEN ... FOR com binds (USING)
    -- -------------------------------------------------------------------------
    FUNCTION fn_buscar_transacoes(
        p_status        IN VARCHAR2    DEFAULT NULL,
        p_parceiro_id   IN NUMBER      DEFAULT NULL,
        p_categoria     IN VARCHAR2    DEFAULT NULL,
        p_data_inicio   IN DATE        DEFAULT NULL,
        p_data_fim      IN DATE        DEFAULT NULL,
        p_valor_minimo  IN NUMBER      DEFAULT NULL,
        p_ordenar_por   IN VARCHAR2    DEFAULT 'DATA',
        p_limite        IN PLS_INTEGER DEFAULT 100
    ) RETURN SYS_REFCURSOR IS
        v_cursor  SYS_REFCURSOR;
        v_sql     VARCHAR2(4000);
        v_limite  PLS_INTEGER;
    BEGIN
        IF p_data_inicio IS NOT NULL AND p_data_fim IS NOT NULL
           AND p_data_inicio > p_data_fim THEN
            pkg_util.sp_erro(pkg_util.c_erro_filtro_invalido,
                             'Data inicial maior que a data final');
        END IF;

        v_limite := LEAST(GREATEST(NVL(p_limite, 100), 1), c_limite_maximo);

        v_sql := fn_sql_busca(p_status, p_parceiro_id, p_categoria,
                              p_data_inicio, p_data_fim, p_valor_minimo, p_ordenar_por);

        -- Os binds sao posicionais: a ordem do USING segue a ordem dos
        -- marcadores no texto montado acima.
        OPEN v_cursor FOR v_sql
            USING UPPER(p_status), p_parceiro_id, p_categoria,
                  TRUNC(p_data_inicio), TRUNC(p_data_fim), p_valor_minimo, v_limite;

        RETURN v_cursor;
    END fn_buscar_transacoes;

    -- -------------------------------------------------------------------------
    -- fn_resumo_transacoes — GROUP BY dinamico
    -- -------------------------------------------------------------------------
    FUNCTION fn_resumo_transacoes(
        p_dimensao  IN VARCHAR2,
        p_status    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR IS
        v_cursor     SYS_REFCURSOR;
        v_expressao  VARCHAR2(100);
        v_sql        VARCHAR2(2000);
    BEGIN
        sp_validar_status(p_status);

        v_expressao := fn_expressao_dimensao(p_dimensao);
        IF v_expressao IS NULL THEN
            pkg_util.sp_erro(pkg_util.c_erro_filtro_invalido,
                             'Dimensão inválida: ' || p_dimensao
                             || ' (use PARCEIRO, CATEGORIA, STATUS ou MES)');
        END IF;

        v_sql := 'SELECT ' || v_expressao || ' AS chave, '
              || 'COUNT(*) AS quantidade, '
              || 'SUM(t.valor) AS total_valor, '
              || 'NVL(SUM(t.cashback_gerado), 0) AS total_cashback '
              || 'FROM transacoes t '
              || 'JOIN parceiros p ON p.id = t.parceiro_id '
              || CASE WHEN p_status IS NOT NULL
                      THEN 'WHERE t.status = :status '
                      ELSE 'WHERE :status IS NULL ' END
              || 'GROUP BY ' || v_expressao || ' '
              || 'ORDER BY total_valor DESC';

        OPEN v_cursor FOR v_sql USING UPPER(p_status);

        RETURN v_cursor;
    END fn_resumo_transacoes;

END pkg_relatorios;
/
