-- =============================================================================
-- Triggers de auditoria e automacao
--
-- Criadas depois da carga simulada (V2) de proposito: a massa de demonstracao
-- nao gera trilha de auditoria falsa nem alertas artificiais.
--
--   trg_auditoria_saldo         AFTER  / ROW  auditoria de toda mudanca de saldo
--   trg_transacao_status        BEFORE / ROW  completa datas ao mudar o status
--   trg_alerta_transacao_erro   AFTER  / ROW  abre alerta quando uma compra falha
-- =============================================================================

-- -----------------------------------------------------------------------------
-- trg_auditoria_saldo — AUDITORIA
--
-- Toda alteracao de saldo (credito de cashback, debito de doacao, ajuste manual
-- feito direto no banco) fica registrada, com quem alterou e por qual programa.
-- Por estar no banco, nenhum caminho consegue mexer no saldo sem deixar rastro.
--
-- Row-level (FOR EACH ROW): dispara uma vez para cada usuario alterado.
-- A clausula WHEN evita registro quando o UPDATE nao muda o valor.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_auditoria_saldo
AFTER UPDATE OF saldo ON usuarios
FOR EACH ROW
WHEN (NEW.saldo <> OLD.saldo)
BEGIN
    INSERT INTO auditoria_saldo (
        usuario_id, saldo_anterior, saldo_novo, diferenca,
        operacao, usuario_banco, modulo
    ) VALUES (
        :NEW.id,
        :OLD.saldo,
        :NEW.saldo,
        :NEW.saldo - :OLD.saldo,
        CASE WHEN :NEW.saldo > :OLD.saldo THEN 'CREDITO' ELSE 'DEBITO' END,
        SYS_CONTEXT('USERENV', 'SESSION_USER'),
        SUBSTR(SYS_CONTEXT('USERENV', 'MODULE'), 1, 64)
    );
END;
/

-- -----------------------------------------------------------------------------
-- trg_transacao_status — AUTOMACAO
--
-- BEFORE: corrige os dados antes de gravar, sem depender de quem fez o UPDATE.
--   * Ao sair de PENDENTE (PROCESSADA ou ERRO), carimba processada_em.
--   * Ao voltar para PENDENTE (reprocessamento), limpa erro, cashback e data,
--     deixando a transacao pronta para a proxima execucao do lote.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_transacao_status
BEFORE UPDATE OF status ON transacoes
FOR EACH ROW
WHEN (NEW.status <> OLD.status)
BEGIN
    IF :NEW.status IN ('PROCESSADA', 'ERRO') THEN
        :NEW.processada_em := NVL(:NEW.processada_em, LOCALTIMESTAMP);
    ELSIF :NEW.status = 'PENDENTE' THEN
        :NEW.processada_em   := NULL;
        :NEW.cashback_gerado := NULL;
        :NEW.erro            := NULL;
    END IF;
END;
/

-- -----------------------------------------------------------------------------
-- trg_alerta_transacao_erro — AUTOMACAO DE ALERTA
--
-- Quando o processamento marca uma compra como ERRO, o alerta aparece no painel
-- na hora, sem esperar a proxima execucao de pkg_monitoramento.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_alerta_transacao_erro
AFTER UPDATE OF status ON transacoes
FOR EACH ROW
WHEN (NEW.status = 'ERRO' AND OLD.status <> 'ERRO')
BEGIN
    INSERT INTO alertas (tipo, severidade, descricao, usuario_id, parceiro_id)
    VALUES (
        'TRANSACAO_COM_ERRO',
        'CRITICO',
        SUBSTR('Compra #' || :NEW.id || ' de R$ ' || pkg_util.fn_moeda_br(:NEW.valor)
               || ' não foi processada: ' || NVL(:NEW.erro, 'motivo não informado'), 1, 400),
        :NEW.usuario_id,
        :NEW.parceiro_id
    );
END;
/
