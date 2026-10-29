# Solidari Backend (Spring Boot + Oracle PL/SQL)

API REST do Solidari: cadastro de usuários, parceiros com cashback, apuração de
crédito e doações convertidas em impacto (árvores plantadas / refeições servidas).

As regras de negócio que mexem em dinheiro ficam no banco, em **packages PL/SQL**.
O Java valida a requisição, cuida da segurança e chama os packages por JDBC.

## Stack

- Java 17 + Spring Boot 3.3 (Spring MVC + Thymeleaf)
- Oracle Database 19c ou superior (Oracle Database Free 23ai em Docker para desenvolvimento)
- Flyway para versionar schema, carga simulada, packages e triggers
- Spring Data JPA (leituras simples) + JDBC (chamada dos packages)
- Spring Security + JWT (autenticação stateless)
- springdoc-openapi (Swagger UI)

## Como rodar

```bash
docker compose up -d oracle        # na raiz do repositório
./gradlew :backend:bootRun
```

A API sobe em `http://localhost:8080` (Swagger em `/swagger-ui.html`). Na primeira
execução o Flyway aplica as migrações `V1` a `V7`.

Conexão configurável por variáveis de ambiente: `SOLIDARI_DB_HOST`,
`SOLIDARI_DB_PORT`, `SOLIDARI_DB_SERVICE`, `SOLIDARI_DB_USER`, `SOLIDARI_DB_PASSWORD`.

## Objetos do banco

| Migração | Conteúdo |
|---|---|
| `V1__schema_inicial.sql` | tabelas, constraints, índices e `auditoria_saldo` |
| `V2__dados_simulados.sql` | carga determinística de parceiros, usuários, compras e doações |
| `V3__pkg_util.sql` | `pkg_util`: códigos de erro e formatação monetária |
| `V4__pkg_carteira.sql` | `pkg_carteira`: doação, impacto, extrato e ranking |
| `V5__pkg_operacoes.sql` | `pkg_cashback` e `pkg_monitoramento`: rotinas em lote |
| `V6__pkg_relatorios_dynamic_sql.sql` | `pkg_relatorios`: busca e resumo com Dynamic SQL |
| `V7__triggers.sql` | `trg_auditoria_saldo`, `trg_transacao_status`, `trg_alerta_transacao_erro` |

## Endpoints principais

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/registrar` | público | Cria usuário e retorna token |
| POST | `/api/auth/login` | público | Autentica e retorna token |
| GET | `/api/usuarios/me` | autenticado | Dados do usuário logado |
| GET | `/api/parceiros` | público | Lista parceiros (filtro `?categoria=`) |
| POST/PUT/DELETE | `/api/parceiros/**` | ADMIN | Manutenção de parceiros |
| POST | `/api/transacoes` | autenticado | Registra compra em parceiro |
| POST | `/api/transacoes/processar-cashback` | ADMIN | `pkg_cashback.sp_processar_cashback` |
| POST | `/api/doacoes` | autenticado | `pkg_carteira.sp_registrar_doacao` |
| GET | `/api/doacoes/impacto` | autenticado | `pkg_carteira.sp_impacto_usuario` |
| GET | `/api/doacoes/extrato` | autenticado | `pkg_carteira.fn_extrato_formatado` |
| GET | `/api/alertas` | ADMIN | Alertas em aberto |
| POST | `/api/alertas/gerar` | ADMIN | `pkg_monitoramento.sp_gerar_alertas` |
| GET | `/api/relatorios/transacoes` | ADMIN | `pkg_relatorios.fn_buscar_transacoes` (Dynamic SQL) |
| GET | `/api/relatorios/resumo` | ADMIN | `pkg_relatorios.fn_resumo_transacoes` (Dynamic SQL) |

## Testes

```bash
./gradlew :backend:test     # requer o Oracle no ar
```

Os mesmos testes rodam no pipeline `.github/workflows/ci-cd.yml`, contra um Oracle
criado só para a execução.

## Imagem Docker

```bash
./gradlew :backend:bootJar
docker build -t solidari-backend backend
```
