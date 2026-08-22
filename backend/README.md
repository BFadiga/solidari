# Solidari Backend (Fase 2 — Spring Boot)

API REST para o app Solidari: cadastro de usuários, parceiros com vantagens de cashback e doações de crédito, com cálculo de impacto (árvores plantadas / refeições servidas), refletindo as telas de Login, Home, Cashback e Localização já existentes no app.

## Stack

- Java 17 + Spring Boot 3.3 (Spring MVC + Thymeleaf)
- Spring Data JPA + H2 (arquivo local, sem infraestrutura externa)
- Spring Security + JWT (autenticação stateless)
- Bean Validation (`jakarta.validation`)
- springdoc-openapi (Swagger UI)

**Por que H2 em vez de Firebase?** O enunciado cita Firebase apenas como exemplo. Como o conteúdo estudado na Fase 1 foi Spring Data JPA, e o objetivo aqui é o mínimo necessário para uma API REST funcional com CRUD, um banco relacional (H2, arquivo local) é suficiente e evita a complexidade extra de configurar credenciais/SDK do Firestore.

## Como rodar

Da raiz do repositório (usa o wrapper Gradle já existente no projeto Android):

```bash
./gradlew :backend:bootRun
```

A API sobe em `http://localhost:8080`.

- Página inicial: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Console do H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/solidari`, usuário `sa`, senha em branco)

Um usuário admin (`admin@solidari.com` / `admin123`) e 3 parceiros de exemplo são criados automaticamente na primeira execução (`config/DataSeeder.java`).

## Autenticação

1. `POST /api/auth/registrar` ou `POST /api/auth/login` retornam um `token` JWT.
2. Envie o token nas requisições protegidas: header `Authorization: Bearer <token>`.
3. Rotas de leitura de parceiros e autenticação são públicas; escrita em parceiros exige papel `ADMIN`; doações e dados do usuário exigem estar autenticado.

## Endpoints principais

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/registrar` | público | Cria usuário e retorna token |
| POST | `/api/auth/login` | público | Autentica e retorna token |
| GET | `/api/usuarios/me` | autenticado | Dados do usuário logado |
| GET | `/api/parceiros` | público | Lista parceiros (filtro `?categoria=`) |
| GET | `/api/parceiros/{id}` | público | Detalhe de um parceiro |
| POST | `/api/parceiros` | ADMIN | Cria parceiro |
| PUT | `/api/parceiros/{id}` | ADMIN | Atualiza parceiro |
| DELETE | `/api/parceiros/{id}` | ADMIN | Remove parceiro |
| POST | `/api/doacoes` | autenticado | Doa crédito do saldo do usuário |
| GET | `/api/doacoes/me` | autenticado | Histórico de doações |
| GET | `/api/doacoes/impacto` | autenticado | Árvores plantadas / refeições servidas |

## Testes

```bash
./gradlew :backend:test
```

Cobrem o fluxo de registro/login e as regras de acesso público x autenticado dos parceiros.
