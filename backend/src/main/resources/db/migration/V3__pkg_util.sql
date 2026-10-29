-- =============================================================================
-- pkg_util — utilitarios compartilhados por todos os packages do Solidari
--
-- Por que um package so para isso:
--   * Os codigos de erro ficam declarados UMA vez, na especificacao. Todo
--     package que levanta erro usa estas constantes, e o Java so precisa
--     conhecer a faixa -20001..-20099 para separar regra de negocio de falha
--     tecnica (ver ErroPlsql.java).
--   * fn_moeda_br e usada por quase todas as rotinas; dentro de um package ela
--     e carregada em memoria uma unica vez por sessao.
-- =============================================================================

CREATE OR REPLACE PACKAGE pkg_util AS

    -- -------------------------------------------------------------------------
    -- Codigos de erro de negocio (RAISE_APPLICATION_ERROR aceita -20000..-20999)
    -- -------------------------------------------------------------------------
    c_erro_saldo_insuficiente  CONSTANT PLS_INTEGER := -20001;
    c_erro_nao_encontrado      CONSTANT PLS_INTEGER := -20002;
    c_erro_valor_invalido      CONSTANT PLS_INTEGER := -20003;
    c_erro_categoria_invalida  CONSTANT PLS_INTEGER := -20004;
    c_erro_parceiro_inativo    CONSTANT PLS_INTEGER := -20005;
    c_erro_rotina              CONSTANT PLS_INTEGER := -20006;
    c_erro_filtro_invalido     CONSTANT PLS_INTEGER := -20007;

    -- Formata valor monetario no padrao brasileiro, sem prefixo (ex.: 1.234,56).
    FUNCTION fn_moeda_br(p_valor IN NUMBER) RETURN VARCHAR2 DETERMINISTIC;

    -- Levanta um erro de negocio padronizado.
    PROCEDURE sp_erro(p_codigo IN PLS_INTEGER, p_mensagem IN VARCHAR2);

END pkg_util;
/

CREATE OR REPLACE PACKAGE BODY pkg_util AS

    -- -------------------------------------------------------------------------
    -- fn_moeda_br
    --
    -- G (grupo) e D (decimal) na mascara seguem NLS_NUMERIC_CHARACTERS. Passando
    -- o parametro NLS direto no TO_CHAR, o resultado nao depende da
    -- configuracao do servidor nem da sessao.
    -- -------------------------------------------------------------------------
    FUNCTION fn_moeda_br(p_valor IN NUMBER) RETURN VARCHAR2 DETERMINISTIC IS
    BEGIN
        IF p_valor IS NULL THEN
            RETURN '0,00';
        END IF;

        RETURN TO_CHAR(p_valor, 'FM999G999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''');
    END fn_moeda_br;

    -- -------------------------------------------------------------------------
    -- sp_erro
    -- -------------------------------------------------------------------------
    PROCEDURE sp_erro(p_codigo IN PLS_INTEGER, p_mensagem IN VARCHAR2) IS
    BEGIN
        RAISE_APPLICATION_ERROR(p_codigo, SUBSTR(p_mensagem, 1, 2000));
    END sp_erro;

END pkg_util;
/
