# Solidari — guia de testes

Este documento mostra como rodar o projeto do zero e testar as funcionalidades nos
dois papéis: **participante**, pelo aplicativo Android, e **administrador**, pelo
painel web.

---

## 1. O que o sistema faz

O Solidari transforma consumo em doação. O ciclo tem quatro etapas:

| # | Etapa | Onde acontece |
|---|---|---|
| 1 | O participante compra em um estabelecimento parceiro | App Android |
| 2 | A compra fica aguardando apuração | — |
| 3 | O administrador apura o cashback em lote | Painel web |
| 4 | O crédito vira doação, que vira árvore ou refeição | App Android |

As regras que sustentam esse ciclo não estão no Java nem no front: são funções e
procedures **PL/pgSQL rodando dentro do PostgreSQL**. O back-end Spring Boot as
aciona por JDBC.

O roteiro da seção 5 percorre as quatro etapas de ponta a ponta.

---

## 2. Preparar o ambiente

### 2.1 Pré-requisitos

| Software | Versão | Verificar com |
|---|---|---|
| JDK | 17 ou 21 | `java -version` |
| PostgreSQL | 14 ou superior | `psql --version` |
| Node.js | 18 ou superior | `node --version` |
| Android Studio | Ladybug ou superior | — |

O Gradle do projeto (8.11) **não funciona com JDK 24 ou 25**. Se o seu `java -version`
mostrar uma dessas, aponte o `JAVA_HOME` para um JDK 17 ou 21 antes de qualquer
comando:

```bash
# macOS com Homebrew (Apple Silicon)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# macOS com Homebrew (Intel)
export JAVA_HOME=/usr/local/opt/openjdk@17

# Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

Não é preciso instalar Gradle nem Angular CLI: o projeto usa o wrapper (`./gradlew`)
e o CLI local do npm.

### 2.2 Banco de dados

Suba o PostgreSQL. No macOS com Homebrew:

```bash
brew install postgresql@17
brew services start postgresql@17
```

Crie o usuário e o banco:

```bash
psql postgres -c "CREATE ROLE solidari WITH LOGIN PASSWORD 'solidari';"
psql postgres -c "CREATE DATABASE solidari OWNER solidari;"
```

**Não crie tabela nenhuma.** Na primeira vez que a API sobe, o Flyway cria o schema,
carrega os dados de exemplo e instala as funções e procedures.

> Se o seu PostgreSQL não estiver na porta 5432, informe a porta ao subir a API:
> `SOLIDARI_DB_PORT=5433 ./gradlew :backend:bootRun`.
> Valem também `SOLIDARI_DB_HOST` e `SOLIDARI_DB_NAME`.

### 2.3 API

Na raiz do repositório:

```bash
./gradlew :backend:bootRun
```

Espere estas duas linhas no log:

```
Successfully applied 4 migrations to schema "public", now at version v4
Started SolidariBackendApplication in 1.7 seconds
```

A API fica em `http://localhost:8080` e a documentação interativa em
`http://localhost:8080/swagger-ui.html`.

**Deixe este terminal aberto.** Tanto o app quanto o painel dependem da API no ar.

### 2.4 Painel web

Em outro terminal:

```bash
cd admin-web
npm install
npm start
```

Acesse `http://localhost:4200`.

### 2.5 Aplicativo Android

Abra a pasta do repositório no Android Studio, escolha um emulador (o projeto foi
testado num Pixel 8, API 35) e rode o módulo `app`.

O app conversa com a API pelo endereço `10.0.2.2:8080`, que é como o **emulador**
enxerga o `localhost` da sua máquina.

> **Testando em celular físico?** `10.0.2.2` não existe fora do emulador. Descubra o
> IP da sua máquina na rede (`ipconfig getifaddr en0` no macOS) e troque a constante
> `BASE_URL` em `app/src/main/java/br/com/fiap/solidarizeapp/data/ApiClient.kt`.
> O celular precisa estar na mesma rede Wi-Fi.

### 2.6 Contas de teste

| Papel | E-mail | Senha |
|---|---|---|
| Administrador | `admin@solidari.com` | `admin123` |
| Participante | `ana.rocha@email.com` | `senha123` |

Existem mais catorze participantes (`bruno.martins@email.com`,
`carla.nogueira@email.com` e outros), todos com a senha `senha123`.

Nenhuma senha é guardada em texto puro: a coluna `usuarios.senha` contém apenas o
hash BCrypt, e o login compara contra ele.

---

## 3. Roteiro A — participante, pelo aplicativo

### 3.1 Login

Abra o app. A primeira tela é o login.

1. Digite `ana.rocha@email.com` e uma senha **errada**. A tela mostra
   *"E-mail ou senha incorretos."* — a recusa vem do servidor, comparando contra o
   hash no banco.
2. Agora use a senha correta, `senha123`. O app entra na Home.

A sessão fica guardada: se você fechar e reabrir o app, ele já entra logado.

### 3.2 Aba Cashback

Toque em **Cashback** na barra inferior.

- **Saldo disponível** — crédito acumulado e ainda não doado.
- **Impacto da Solidariedade** — árvores e refeições. Esses números **não** são
  calculados no app nem no Java: vêm da função `fn_impacto_usuario`.
- **Vantagens dos Parceiros** — os doze parceiros vindos da API, com o percentual de
  cada um. Os chips de categoria filtram a lista; toque no mesmo chip de novo para
  desmarcar.

### 3.3 Doar crédito

Toque em **Doar Crédito**.

1. Informe um valor **menor que o saldo**, escolha *Restauração ambiental* e
   confirme. O saldo cai na hora e as árvores sobem — a cada R$ 10 doados,
   uma árvore.
2. Abra o diálogo de novo e tente doar um valor **maior que o saldo**. O botão
   *Confirmar* fica desabilitado, e mesmo que você force, a resposta do servidor é a
   mensagem levantada pelo `RAISE EXCEPTION` do PL/pgSQL:

   > Saldo insuficiente: disponível R$ 65,41, solicitado R$ 99.999,00

A doação percorre o caminho completo: toque no app → `POST /api/doacoes` → Java →
JDBC → procedure `sp_registrar_doacao`, que valida o saldo, debita e grava numa
única operação atômica.

### 3.4 Aba Localização

Os doze parceiros aparecem no mapa **posicionados pelas coordenadas reais**
guardadas no banco. Toque num marcador para ver nome, descrição, percentual e
categoria. A busca filtra por nome enquanto você digita, e os chips filtram por
categoria.

### 3.5 Aba Perfil

Nome, e-mail, saldo, árvores, refeições e as doações recentes — todos vindos da API
para o usuário autenticado.

### 3.6 Aba Opções

Aqui ficam as ações de conta:

- **Registrar compra em parceiro** — escolha o parceiro, informe o valor e o app
  mostra quanto voltará. A compra entra como *Aguardando*: o crédito só existe
  depois que o administrador apurar.
- **Extrato de doações** — mostra sua posição no ranking (calculada por
  `fn_ranking_doador`) e expande para o texto produzido por `fn_extrato_formatado`.
  Os valores em `R$ 1.234,56` e as datas em `DD/MM/AAAA` são formatados **dentro do
  PostgreSQL**, não pelo app.
- **Minhas compras** — histórico com o cashback creditado em cada uma.

---

## 4. Roteiro B — administrador, pelo painel web

Acesse `http://localhost:4200` e entre com `admin@solidari.com` / `admin123`. Há
botões que preenchem as contas de demonstração com um clique.

### 4.1 Controle de acesso

Antes de logar, abra a aba **Parceiros**. A listagem aparece, mas não há botões de
administração — a leitura é pública, a escrita não. Entre como participante
(`ana.rocha@email.com`) e verá o mesmo: a tela avisa *"Modo somente leitura"*.

Só com a conta de administrador as ações aparecem.

### 4.2 Apurar cashback

Na aba **Parceiros**, cartão **Apurar cashback**, clique em *Apurar agora*.

O resultado aparece na tela:

> 21 compras creditadas · R$ 358,18 distribuídos

Isso executa a procedure `sp_processar_cashback`, que percorre as compras pendentes
com um cursor, calcula o crédito pelo percentual de cada parceiro e soma ao saldo do
participante. Se uma compra falhar, ela é marcada como erro e o lote continua.

### 4.3 Verificar pendências

Clique em *Verificar agora*. A procedure `sp_gerar_alertas` varre o sistema e
registra o que precisa de atenção:

- crédito parado (participante com saldo relevante sem doar há mais de 60 dias);
- parceiro sem movimento nos últimos 45 dias;
- compras que falharam na apuração.

Os alertas aparecem logo abaixo, ordenados por severidade.

### 4.4 Gerenciar parceiros

Cadastre, edite e remova parceiros. O campo **Cashback (%)** é o que alimenta o
cálculo do crédito — o selo ao lado é apenas o rótulo exibido no aplicativo.

Dois comportamentos que valem verificar:

- Tente **remover um parceiro que já tem compras**, como o Green Leaf Cafe. A
  operação é recusada com uma mensagem explicando o motivo, em vez de quebrar por
  violação de chave estrangeira.
- Cadastre um parceiro novo com 25% de cashback e confira que ele aparece
  imediatamente na lista do aplicativo.

### 4.5 Carteira no painel

A aba **Carteira** repete, no navegador, o que o participante vê no app: saldo,
impacto, extrato, doação e registro de compra. Ela existe como atalho de testes —
a experiência do participante de verdade é o aplicativo.

---

## 5. Roteiro C — o ciclo completo

Este é o roteiro que demonstra o sistema inteiro, e é o que vale gravar em vídeo.
Deixe o app e o painel abertos lado a lado.

**Passo 1 — no aplicativo, como Ana.**
Vá em *Opções* → *Registrar compra em parceiro*. Escolha **Cursinho Aurora**, que
devolve 20%, e informe **150,00**. O app avisa que você receberá cerca de R$ 30,00.

Confira em *Minhas compras*: a compra está como **Aguardando**, sem cashback. Anote
o saldo atual.

**Passo 2 — no painel, como administrador.**
Aba *Parceiros* → *Apurar agora*. O painel informa quantas compras foram creditadas
e o total distribuído.

**Passo 3 — de volta ao aplicativo.**
Puxe a aba *Opções* de novo. A compra virou **+ R$ 30,00** e o saldo subiu
exatamente esse valor. Vinte por cento de R$ 150,00.

**Passo 4 — doe o crédito.**
Aba *Cashback* → *Doar Crédito* → R$ 30,00 em *Restauração ambiental*. O saldo cai,
e o contador de árvores sobe em três — porque cada R$ 10 plantam uma.

O crédito que nasceu de uma compra virou impacto, e cada etapa passou por uma rotina
PL/pgSQL diferente.

---

## 6. Onde cada rotina do banco entra

| Rotina | Tipo | Acionada por |
|---|---|---|
| `fn_impacto_usuario` | function | Cashback e Perfil no app, Carteira no painel |
| `fn_extrato_formatado` | function | *Extrato de doações*, em Opções |
| `fn_ranking_doador` | function | posição exibida junto ao extrato |
| `fn_moeda_br` | function | formatação usada pelas demais |
| `sp_registrar_doacao` | procedure | botão *Doar Crédito* |
| `sp_processar_cashback` | procedure | botão *Apurar agora*, no painel |
| `sp_gerar_alertas` | procedure | botão *Verificar agora*, no painel |

---

## 7. Testar as rotinas direto no banco

As funções e procedures funcionam fora da aplicação, em qualquer cliente SQL:

```bash
PGPASSWORD=solidari psql -h localhost -U solidari -d solidari
```

```sql
-- Indicador de impacto
SELECT * FROM fn_impacto_usuario(2);

-- Extrato formatado
SELECT fn_extrato_formatado(2, 5);

-- Formatação monetária brasileira
SELECT fn_moeda_br(1234567.89);

-- Função usada dentro de uma consulta comum: os dez maiores doadores
SELECT u.nome, fn_ranking_doador(u.id) AS posicao
FROM usuarios u
WHERE fn_ranking_doador(u.id) BETWEEN 1 AND 10
ORDER BY posicao;

-- Procedures (os NULL são os parâmetros de saída)
CALL sp_processar_cashback(500, NULL, NULL, NULL);
CALL sp_gerar_alertas(NULL);
CALL sp_registrar_doacao(2, 1, 25.00, 'RESTAURACAO_AMBIENTAL', NULL);

-- Regra de negócio recusando a operação
CALL sp_registrar_doacao(2, 1, 999999.00, 'RESTAURACAO_AMBIENTAL', NULL);
```

Para ver o código-fonte de qualquer rotina:

```sql
\df+ fn_impacto_usuario
SELECT prosrc FROM pg_proc WHERE proname = 'sp_processar_cashback';
```

---

## 8. Testar a API por linha de comando

Guarde o token numa variável:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana.rocha@email.com","senha":"senha123"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
```

```bash
# Indicador calculado no banco
curl -s localhost:8080/api/doacoes/impacto -H "Authorization: Bearer $TOKEN"
# {"arvoresPlantadas":4,"refeicoesServidas":13,"totalDoado":116.60}

# Extrato formatado pelo PostgreSQL
curl -s "localhost:8080/api/doacoes/extrato?limite=3" -H "Authorization: Bearer $TOKEN"

# Doação (aciona sp_registrar_doacao)
curl -s -X POST localhost:8080/api/doacoes \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"valor":30.00,"categoriaImpacto":"RESTAURACAO_AMBIENTAL","parceiroId":1}'

# Regra de negócio: 422 com a mensagem do RAISE
curl -s -X POST localhost:8080/api/doacoes \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"valor":99999,"categoriaImpacto":"RESTAURACAO_AMBIENTAL","parceiroId":1}'
```

---

## 9. Testes automatizados

```bash
./gradlew :backend:test
```

São doze testes. Oito exercitam as rotinas PL/pgSQL pela mesma camada JDBC que a
API usa: débito de saldo, recusa por saldo insuficiente, conversão em impacto,
processamento de cashback e formatação de valores.

Os testes rodam contra o mesmo banco de desenvolvimento e usam `@Transactional`,
então tudo o que gravam é desfeito ao final — os dados de exemplo permanecem
intactos.

---

## 10. Recriar o banco do zero

A qualquer momento, apague o schema e suba a API de novo. O Flyway refaz tudo:

```bash
PGPASSWORD=solidari psql -h localhost -U solidari -d solidari \
  -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

./gradlew :backend:bootRun
```

A massa de dados é gerada de forma determinística: qualquer máquina produz
exatamente os mesmos usuários, compras e doações.

---

## 11. Problemas comuns

**O Gradle falha logo no início, ou acusa versão de classe não suportada**
Está rodando sobre um JDK 24/25. Exporte o `JAVA_HOME` conforme a seção 2.1.

**`Connection to localhost:5432 refused`**
O PostgreSQL não está no ar (`brew services start postgresql@17`) ou está em outra
porta. Descubra com `lsof -nP -iTCP -sTCP:LISTEN | grep postgres` e informe via
`SOLIDARI_DB_PORT`.

**`password authentication failed for user "solidari"`**
O usuário do banco não foi criado. Refaça os comandos da seção 2.2.

**No aplicativo: "Sem conexão com o servidor"**
A API não está no ar, ou o endereço está errado. No emulador o endereço tem que ser
`10.0.2.2`, nunca `localhost` — `localhost` dentro do emulador é o próprio Android.
Em celular físico, troque pelo IP da máquina (seção 2.5).

**No painel: "Não foi possível carregar os parceiros"**
A API não está respondendo em `localhost:8080`. Confira o terminal da seção 2.3.

**`Port 8080 was already in use`**
Suba em outra porta com `./gradlew :backend:bootRun --args='--server.port=8081'` —
e lembre de ajustar `admin-web/src/environments/environment.ts` e o `BASE_URL` do
app.

**`Validation failed: schema is not up to date` ou erro de checksum do Flyway**
O schema divergiu das migrações. Recrie o banco conforme a seção 10.
