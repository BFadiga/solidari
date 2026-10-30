-- =============================================================================
-- pkg_carteira — doacoes e indicadores de impacto do usuario
--
-- Agrupa tudo o que mexe com a carteira de credito: a procedure que registra
-- a doacao e as functions de leitura (impacto, extrato, ranking).
--
-- Especificacao (publica):  o que o back-end Java enxerga e pode chamar.
-- Corpo (privado):          a implementacao, que pode mudar sem quebrar quem
--                           chama, desde que a especificacao continue igual.
--
-- Nenhuma rotina faz COMMIT: quem controla a transacao e o chamador (o Spring,
-- no back-end). Isso mantem a doacao atomica junto com o resto da requisicao e
-- permite que os testes desfacam tudo ao final.
-- =============================================================================

CREATE OR REPLACE PACKAGE pkg_carteira AS

    -- -------------------------------------------------------------------------
    -- Variaveis globais (constantes) do package: valem para todas as rotinas
    -- abaixo e podem ser lidas de fora, em PL/SQL (ex.: pkg_carteira.c_valor_arvore).
    -- Mudar o "preco" de uma arvore ou refeicao e alterar um unico lugar.
    -- -------------------------------------------------------------------------
    c_valor_arvore    CONSTANT NUMBER := 10.00;   -- R$ por arvore plantada
    c_valor_refeicao  CONSTANT NUMBER := 5.00;    -- R$ por refeicao servida

    -- Registra uma doacao de forma atomica: valida, debita a carteira e grava.
    PROCEDURE sp_registrar_doacao(
        p_usuario_id   IN  NUMBER,
        p_parceiro_id  IN  NUMBER,
        p_valor        IN  NUMBER,
        p_categoria    IN  VARCHAR2,
        p_doacao_id    OUT NUMBER
    );

    -- Indicador de impacto: arvores plantadas, refeicoes servidas e total doado.
    PROCEDURE sp_impacto_usuario(
        p_usuario_id   IN  NUMBER,
        p_arvores      OUT NUMBER,
        p_refeicoes    OUT NUMBER,
        p_total_doado  OUT NUMBER
    );

    -- Extrato de doacoes formatado em pt-BR (R$ e DD/MM/AAAA).
    FUNCTION fn_extrato_formatado(
        p_usuario_id IN NUMBER,
        p_limite     IN PLS_INTEGER DEFAULT 10
    ) RETURN CLOB;

    -- Posicao no ranking por total doado (1 = maior doador; 0 = ainda nao doou).
    FUNCTION fn_ranking_doador(p_usuario_id IN NUMBER) RETURN NUMBER;

END pkg_carteira;
/

CREATE OR REPLACE PACKAGE BODY pkg_carteira AS

    -- Constante privada: so existe dentro do corpo.
    c_nl CONSTANT VARCHAR2(1) := CHR(10);

    -- -------------------------------------------------------------------------
    -- sp_registrar_doacao — acionada pelo back-end (REST -> Java -> JDBC -> banco)
    -- -------------------------------------------------------------------------
    PROCEDURE sp_registrar_doacao(
        p_usuario_id   IN  NUMBER,
        p_parceiro_id  IN  NUMBER,
        p_valor        IN  NUMBER,
        p_categoria    IN  VARCHAR2,
        p_doacao_id    OUT NUMBER
    ) IS
        v_saldo   usuarios.saldo%TYPE;
        v_existe  PLS_INTEGER;
    BEGIN
        IF p_valor IS NULL OR p_valor <= 0 THEN
            pkg_util.sp_erro(pkg_util.c_erro_valor_invalido,
                             'Valor da doação deve ser maior que zero');
        END IF;

        IF p_categoria IS NULL
           OR p_categoria NOT IN ('RESTAURACAO_AMBIENTAL', 'BEM_ESTAR_COMUNITARIO') THEN
            pkg_util.sp_erro(pkg_util.c_erro_categoria_invalida,
                             'Categoria de impacto inválida: ' || p_categoria);
        END IF;

        -- FOR UPDATE trava a linha do usuario ate o fim da transacao: duas
        -- doacoes simultaneas nao conseguem ler o mesmo saldo e gastar duas vezes.
        BEGIN
            SELECT saldo
              INTO v_saldo
              FROM usuarios
             WHERE id = p_usuario_id
               FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                pkg_util.sp_erro(pkg_util.c_erro_nao_encontrado,
                                 'Usuário ' || p_usuario_id || ' não encontrado');
        END;

        IF v_saldo < p_valor THEN
            pkg_util.sp_erro(pkg_util.c_erro_saldo_insuficiente,
                             'Saldo insuficiente: disponível R$ ' || pkg_util.fn_moeda_br(v_saldo)
                             || ', solicitado R$ ' || pkg_util.fn_moeda_br(p_valor));
        END IF;

        IF p_parceiro_id IS NOT NULL THEN
            SELECT COUNT(*) INTO v_existe FROM parceiros WHERE id = p_parceiro_id;

            IF v_existe = 0 THEN
                pkg_util.sp_erro(pkg_util.c_erro_nao_encontrado,
                                 'Parceiro ' || p_parceiro_id || ' não encontrado');
            END IF;
        END IF;

        -- O debito dispara trg_auditoria_saldo, que registra a operacao.
        UPDATE usuarios
           SET saldo = saldo - p_valor
         WHERE id = p_usuario_id;

        INSERT INTO doacoes (usuario_id, parceiro_id, valor, categoria_impacto, data)
        VALUES (p_usuario_id, p_parceiro_id, p_valor, p_categoria, LOCALTIMESTAMP)
        RETURNING id INTO p_doacao_id;
    END sp_registrar_doacao;

    -- -------------------------------------------------------------------------
    -- sp_impacto_usuario
    --
    -- Usuario inexistente e erro de chamada, nao "impacto zero": sem esta
    -- checagem um id errado devolveria 0/0/0 silenciosamente.
    -- -------------------------------------------------------------------------
    PROCEDURE sp_impacto_usuario(
        p_usuario_id   IN  NUMBER,
        p_arvores      OUT NUMBER,
        p_refeicoes    OUT NUMBER,
        p_total_doado  OUT NUMBER
    ) IS
        v_existe       PLS_INTEGER;
        v_ambiental    NUMBER;
        v_comunitario  NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_existe FROM usuarios WHERE id = p_usuario_id;

        IF v_existe = 0 THEN
            pkg_util.sp_erro(pkg_util.c_erro_nao_encontrado,
                             'Usuário ' || p_usuario_id || ' não encontrado');
        END IF;

        SELECT NVL(SUM(CASE WHEN categoria_impacto = 'RESTAURACAO_AMBIENTAL' THEN valor END), 0),
               NVL(SUM(CASE WHEN categoria_impacto = 'BEM_ESTAR_COMUNITARIO' THEN valor END), 0)
          INTO v_ambiental, v_comunitario
          FROM doacoes
         WHERE usuario_id = p_usuario_id;

        p_arvores     := FLOOR(v_ambiental   / c_valor_arvore);
        p_refeicoes   := FLOOR(v_comunitario / c_valor_refeicao);
        p_total_doado := ROUND(v_ambiental + v_comunitario, 2);
    EXCEPTION
        WHEN ZERO_DIVIDE THEN
            -- Protege contra alteracao futura das constantes para zero.
            pkg_util.sp_erro(pkg_util.c_erro_rotina,
                             'Constante de conversão de impacto inválida (divisão por zero)');
    END sp_impacto_usuario;

    -- -------------------------------------------------------------------------
    -- fn_extrato_formatado — percorre as doacoes com CURSOR explicito
    -- -------------------------------------------------------------------------
    FUNCTION fn_extrato_formatado(
        p_usuario_id IN NUMBER,
        p_limite     IN PLS_INTEGER DEFAULT 10
    ) RETURN CLOB IS

        CURSOR c_doacoes (cp_usuario_id NUMBER, cp_limite PLS_INTEGER) IS
            SELECT d.data,
                   d.valor,
                   d.categoria_impacto,
                   NVL(p.nome, 'Doação direta') AS parceiro
              FROM doacoes d
              LEFT JOIN parceiros p ON p.id = d.parceiro_id
             WHERE d.usuario_id = cp_usuario_id
             ORDER BY d.data DESC, d.id DESC
             FETCH FIRST cp_limite ROWS ONLY;

        v_linha     c_doacoes%ROWTYPE;
        v_saida     CLOB;
        v_nome      usuarios.nome%TYPE;
        v_saldo     usuarios.saldo%TYPE;
        v_total     NUMBER := 0;
        v_contador  PLS_INTEGER := 0;
    BEGIN
        SELECT nome, saldo INTO v_nome, v_saldo
          FROM usuarios
         WHERE id = p_usuario_id;

        v_saida := 'EXTRATO SOLIDARI — ' || v_nome || c_nl
                || RPAD('-', 58, '-') || c_nl;

        OPEN c_doacoes(p_usuario_id, NVL(p_limite, 10));
        LOOP
            FETCH c_doacoes INTO v_linha;
            EXIT WHEN c_doacoes%NOTFOUND;

            v_contador := v_contador + 1;
            v_total    := v_total + v_linha.valor;

            v_saida := v_saida
                    || TO_CHAR(v_linha.data, 'DD/MM/YYYY') || '  '
                    || RPAD(SUBSTR(v_linha.parceiro, 1, 22), 22) || '  '
                    || LPAD('R$ ' || pkg_util.fn_moeda_br(v_linha.valor), 14) || '  '
                    || CASE v_linha.categoria_impacto
                           WHEN 'RESTAURACAO_AMBIENTAL' THEN 'Ambiental'
                           ELSE 'Comunitário'
                       END
                    || c_nl;
        END LOOP;
        CLOSE c_doacoes;

        IF v_contador = 0 THEN
            RETURN v_saida || 'Nenhuma doação registrada até o momento.' || c_nl;
        END IF;

        RETURN v_saida
            || RPAD('-', 58, '-') || c_nl
            || v_contador || ' doação(ões)   Total: R$ ' || pkg_util.fn_moeda_br(v_total)
            || '   Saldo atual: R$ ' || pkg_util.fn_moeda_br(v_saldo) || c_nl;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- SELECT INTO sem linha: id inexistente. Tratado aqui porque, dentro
            -- de um SELECT, NO_DATA_FOUND nao propagado viraria NULL silencioso.
            RETURN 'Usuário ' || p_usuario_id || ' não encontrado.';
        WHEN OTHERS THEN
            -- O cursor pode ter ficado aberto se o erro ocorreu dentro do LOOP.
            IF c_doacoes%ISOPEN THEN
                CLOSE c_doacoes;
            END IF;
            RAISE;
    END fn_extrato_formatado;

    -- -------------------------------------------------------------------------
    -- fn_ranking_doador — feita para ser usada dentro de consultas SQL comuns
    -- -------------------------------------------------------------------------
    FUNCTION fn_ranking_doador(p_usuario_id IN NUMBER) RETURN NUMBER IS
        v_posicao NUMBER;
    BEGIN
        SELECT r.posicao
          INTO v_posicao
          FROM (SELECT usuario_id,
                       RANK() OVER (ORDER BY SUM(valor) DESC) AS posicao
                  FROM doacoes
                 GROUP BY usuario_id) r
         WHERE r.usuario_id = p_usuario_id;

        RETURN v_posicao;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Usuario sem doacao nao aparece no agrupamento: fora do ranking.
            RETURN 0;
    END fn_ranking_doador;

END pkg_carteira;
/
