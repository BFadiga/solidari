-- Relatorio do banco gerado pelo pipeline (executado com SQL*Plus no container Oracle).
SET PAGESIZE 200
SET LINESIZE 200
SET FEEDBACK OFF
SET TRIMSPOOL ON
SET HEADING ON

PROMPT === Migracoes aplicadas pelo Flyway ===
COLUMN version     FORMAT A8
COLUMN description FORMAT A40
COLUMN sucesso     FORMAT A7
SELECT "version" AS version,
       "description" AS description,
       CASE "success" WHEN 1 THEN 'OK' ELSE 'FALHOU' END AS sucesso,
       "execution_time" AS ms
  FROM "flyway_schema_history"
 ORDER BY "installed_rank";

PROMPT
PROMPT === Objetos PL/SQL (packages e triggers) ===
COLUMN object_name FORMAT A32
COLUMN object_type FORMAT A14
COLUMN status      FORMAT A8
SELECT object_name, object_type, status
  FROM user_objects
 WHERE object_type IN ('PACKAGE', 'PACKAGE BODY', 'TRIGGER')
 ORDER BY object_type, object_name;

PROMPT
PROMPT === Volume de dados ===
SELECT 'usuarios'        AS tabela, COUNT(*) AS linhas FROM usuarios
UNION ALL SELECT 'parceiros',       COUNT(*) FROM parceiros
UNION ALL SELECT 'transacoes',      COUNT(*) FROM transacoes
UNION ALL SELECT 'doacoes',         COUNT(*) FROM doacoes
UNION ALL SELECT 'alertas',         COUNT(*) FROM alertas
UNION ALL SELECT 'auditoria_saldo', COUNT(*) FROM auditoria_saldo;

PROMPT
SET HEADING OFF
SELECT 'OBJETOS_INVALIDOS=' || COUNT(*) FROM user_objects WHERE status <> 'VALID';
EXIT
