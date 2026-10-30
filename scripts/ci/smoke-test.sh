#!/usr/bin/env bash
# Smoke test da API publicada pelo pipeline.
# Exercita o caminho REST -> Java -> JDBC -> packages PL/SQL no Oracle.
#
# Uso: smoke-test.sh <url-base> <arquivo-relatorio.md>
set -uo pipefail

BASE_URL="${1:-http://localhost:8080}"
RELATORIO="${2:-smoke-test.md}"
FALHAS=0

echo "| Verificação | Esperado | Obtido | Resultado |" > "$RELATORIO"
echo "|---|---|---|---|" >> "$RELATORIO"

registrar() {
  local nome="$1" esperado="$2" obtido="$3"
  if [[ "$esperado" == "$obtido" ]]; then
    echo "| $nome | $esperado | $obtido | ✅ |" >> "$RELATORIO"
    echo "[OK]    $nome ($obtido)"
  else
    echo "| $nome | $esperado | $obtido | ❌ |" >> "$RELATORIO"
    echo "[FALHA] $nome: esperado $esperado, obtido $obtido"
    FALHAS=$((FALHAS + 1))
  fi
}

# status HTTP de uma chamada; corpo salvo em /tmp/resposta.json
chamar() {
  local metodo="$1" caminho="$2" token="${3:-}" corpo="${4:-}"
  local args=(-s -o /tmp/resposta.json -w "%{http_code}" -X "$metodo" "$BASE_URL$caminho")
  [[ -n "$token" ]] && args+=(-H "Authorization: Bearer $token")
  [[ -n "$corpo" ]] && args+=(-H "Content-Type: application/json" -d "$corpo")
  curl "${args[@]}"
}

# 1. Catalogo publico de parceiros (carga simulada do Flyway)
registrar "GET /api/parceiros" "200" "$(chamar GET /api/parceiros)"
registrar "Parceiros carregados" "12" "$(jq 'length' /tmp/resposta.json)"

# 2. Autenticacao
registrar "Login com senha errada" "401" \
  "$(chamar POST /api/auth/login '' '{"email":"admin@solidari.com","senha":"errada"}')"
registrar "Login do admin" "200" \
  "$(chamar POST /api/auth/login '' '{"email":"admin@solidari.com","senha":"admin123"}')"
TOKEN="$(jq -r '.token' /tmp/resposta.json)"

registrar "Rota admin sem token" "403" "$(chamar POST /api/transacoes/processar-cashback)"

# 3. Packages PL/SQL
registrar "pkg_cashback.sp_processar_cashback" "200" \
  "$(chamar POST /api/transacoes/processar-cashback "$TOKEN")"
echo "    resultado: $(cat /tmp/resposta.json)"

registrar "pkg_monitoramento.sp_gerar_alertas" "200" "$(chamar POST /api/alertas/gerar "$TOKEN")"
echo "    resultado: $(cat /tmp/resposta.json)"

# 4. Dynamic SQL (pkg_relatorios)
registrar "Busca dinâmica (VALOR, limite 5)" "200" \
  "$(chamar GET '/api/relatorios/transacoes?status=PROCESSADA&ordenarPor=VALOR&limite=5' "$TOKEN")"
registrar "Busca respeita o limite" "true" "$(jq 'length <= 5' /tmp/resposta.json)"

registrar "Resumo dinâmico por STATUS" "200" \
  "$(chamar GET '/api/relatorios/resumo?dimensao=STATUS' "$TOKEN")"

registrar "Ordenação fora da lista é recusada" "422" \
  "$(chamar GET '/api/relatorios/transacoes?ordenarPor=senha' "$TOKEN")"

echo
if [[ "$FALHAS" -gt 0 ]]; then
  echo "Smoke test: $FALHAS verificação(ões) falharam."
  exit 1
fi
echo "Smoke test: todas as verificações passaram."
