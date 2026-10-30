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

As regras que sustentam esse ciclo não estão no Java nem no front: são **packages
PL/SQL rodando dentro do Oracle**, mais três triggers de auditoria e automação. O
back-end Spring Boot aciona os packages por JDBC.

O roteiro da seção 5 percorre as quatro etapas de ponta a ponta.

---

## 2. Preparar o ambiente

### 2.1 Pré-requisitos

| Software | Versão | Verificar com |
|---|---|---|
| JDK | 17 ou 21 | `java -version` |
| Docker | 24 ou superior | `docker --version` |
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

### 2.2 Banco de dados (Oracle)

O jeito mais simples é o **Oracle Database Free** em Docker, já descrito no
`docker-compose.yml` da raiz:

```bash
docker compose up -d oracle
```

Na primeira vez o download da imagem leva alguns minutos. O banco está pronto quando
`docker compose ps` mostra o serviço como `healthy`. O usuário de aplicação
(`solidari` / `solidari`, serviço `FREEPDB1`) é criado automaticamente.

**Não crie tabela nenhuma.** Na primeira vez que a API sobe, o Flyway cria o schema,
carrega os dados de exemplo e instala os packages e as triggers.

> **Preferindo o Oracle da FIAP?** Aponte a API para ele com variáveis de ambiente:
> `SOLIDARI_DB_HOST=oracle.fiap.com.br SOLIDARI_DB_SERVICE=ORCL
> SOLIDARI_DB_USER=rmXXXXX SOLIDARI_DB_PASSWORD=<sua senha> ./gradlew :backend:bootRun`.
> Valem também `SOLIDARI_DB_PORT` (padrão 1521). Confira host e serviço no material
> da disciplina. O schema é compatível com Oracle 19c ou superior.

### 2.3 API

Na raiz do repositório:

```bash
./gradlew :backend:bootRun
```

Espere estas duas linhas no log:

```
Successfully applied 7 migrations to schema "SOLIDARI", now at version v7
Started SolidariBackendApplication in X.X seconds
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
  calculados no app nem no Java: vêm de `pkg_carteira.sp_impacto_usuario`.
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
   mensagem levantada pelo `RAISE_APPLICATION_ERROR` do package PL/SQL:

   > Saldo insuficiente: disponível R$ 65,41, solicitado R$ 99.999,00

A doação percorre o caminho completo: toque no app → `POST /api/doacoes` → Java →
JDBC → `pkg_carteira.sp_registrar_doacao`, que valida o saldo, debita e grava numa
única operação atômica. O débito dispara a trigger `trg_auditoria_saldo`, que
registra a operação em `auditoria_saldo`.

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
  `pkg_carteira.fn_ranking_doador`) e expande para o texto produzido por
  `pkg_carteira.fn_extrato_formatado`. Os valores em `R$ 1.234,56` e as datas em
  `DD/MM/AAAA` são formatados **dentro do Oracle**, não pelo app.
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

Isso executa `pkg_cashback.sp_processar_cashback`, que percorre as compras pendentes
com um cursor, calcula o crédito pelo percentual de cada parceiro e soma ao saldo do
participante. Se uma compra falhar, ela volta ao `SAVEPOINT` do item, é marcada como
erro e o lote continua; a trigger `trg_alerta_transacao_erro` abre um alerta na hora.

### 4.3 Verificar pendências

Clique em *Verificar agora*. `pkg_monitoramento.sp_gerar_alertas` varre o sistema
e registra o que precisa de atenção (sem duplicar alertas que ainda estão abertos):

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

O crédito que nasceu de uma compra virou impacto, e cada etapa passou por um package
PL/SQL diferente.

---

## 6. Onde cada objeto do banco entra

| Objeto | Tipo | Acionado por |
|---|---|---|
| `pkg_carteira.sp_impacto_usuario` | procedure | Cashback e Perfil no app, Carteira no painel |
| `pkg_carteira.fn_extrato_formatado` | function | *Extrato de doações*, em Opções |
| `pkg_carteira.fn_ranking_doador` | function | posição exibida junto ao extrato |
| `pkg_carteira.sp_registrar_doacao` | procedure | botão *Doar Crédito* |
| `pkg_cashback.sp_processar_cashback` | procedure | botão *Apurar agora*, no painel |
| `pkg_monitoramento.sp_gerar_alertas` | procedure | botão *Verificar agora*, no painel |
| `pkg_relatorios.fn_buscar_transacoes` | function (Dynamic SQL) | `GET /api/relatorios/transacoes` |
| `pkg_relatorios.fn_resumo_transacoes` | function (Dynamic SQL) | `GET /api/relatorios/resumo` |
| `pkg_util.fn_moeda_br` | function | formatação usada pelos demais |
| `trg_auditoria_saldo` | trigger | toda alteração de saldo |
| `trg_transacao_status` | trigger | toda mudança de status de compra |
| `trg_alerta_transacao_erro` | trigger | compra marcada como ERRO |

---

## 7. Testar os packages direto no banco

Os packages funcionam fora da aplicação, em qualquer cliente SQL (SQL Developer,
SQLcl ou o `sqlplus` do próprio container):

```bash
docker exec -it solidari-oracle sqlplus solidari/solidari@//localhost/FREEPDB1
```

```sql
SET SERVEROUTPUT ON

-- Formatação monetária brasileira
SELECT pkg_util.fn_moeda_br(1234567.89) FROM dual;

-- Extrato formatado
SELECT pkg_carteira.fn_extrato_formatado(2, 5) FROM dual;

-- Function usada dentro de uma consulta comum: os dez maiores doadores
SELECT u.nome, pkg_carteira.fn_ranking_doador(u.id) AS posicao
  FROM usuarios u
 WHERE pkg_carteira.fn_ranking_doador(u.id) BETWEEN 1 AND 10
 ORDER BY posicao;

-- Procedures com parâmetros de saída
DECLARE
    v_arvores NUMBER; v_refeicoes NUMBER; v_total NUMBER;
    v_proc PLS_INTEGER; v_erro PLS_INTEGER; v_credito NUMBER;
BEGIN
    pkg_carteira.sp_impacto_usuario(2, v_arvores, v_refeicoes, v_total);
    DBMS_OUTPUT.PUT_LINE(v_arvores || ' árvores, ' || v_refeicoes || ' refeições');

    pkg_cashback.sp_processar_cashback(500, v_proc, v_erro, v_credito);
END;
/

-- Trilha de auditoria gerada pela trigger
SELECT usuario_id, operacao, diferenca, saldo_novo, modulo, registrado_em
  FROM auditoria_saldo ORDER BY id DESC FETCH FIRST 10 ROWS ONLY;

-- Dynamic SQL: veja o comando que o package monta para um filtro
SELECT pkg_relatorios.fn_sql_busca(p_status => 'ERRO', p_valor_minimo => 100) FROM dual;

-- Regra de negócio recusando a operação (ORA-20001)
DECLARE v_id NUMBER;
BEGIN
    pkg_carteira.sp_registrar_doacao(2, 1, 999999, 'RESTAURACAO_AMBIENTAL', v_id);
END;
/
ROLLBACK;
```

Para ver o código-fonte de qualquer objeto:

```sql
SELECT text FROM user_source WHERE name = 'PKG_CASHBACK' AND type = 'PACKAGE BODY' ORDER BY line;
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

# Extrato formatado pelo Oracle
curl -s "localhost:8080/api/doacoes/extrato?limite=3" -H "Authorization: Bearer $TOKEN"

# Doação (aciona pkg_carteira.sp_registrar_doacao)
curl -s -X POST localhost:8080/api/doacoes \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"valor":30.00,"categoriaImpacto":"RESTAURACAO_AMBIENTAL","parceiroId":1}'

# Regra de negócio: 422 com a mensagem do RAISE_APPLICATION_ERROR
curl -s -X POST localhost:8080/api/doacoes \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"valor":99999,"categoriaImpacto":"RESTAURACAO_AMBIENTAL","parceiroId":1}'
```

Relatórios com Dynamic SQL (exigem login de administrador):

```bash
ADMIN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@solidari.com","senha":"admin123"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

# Só os filtros informados entram no SQL montado pelo package
curl -s "localhost:8080/api/relatorios/transacoes?status=PROCESSADA&valorMinimo=200&ordenarPor=VALOR&limite=5" \
  -H "Authorization: Bearer $ADMIN"

# Agrupamento escolhido em tempo de execução: PARCEIRO, CATEGORIA, STATUS ou MES
curl -s "localhost:8080/api/relatorios/resumo?dimensao=CATEGORIA" -H "Authorization: Bearer $ADMIN"

# Ordenação fora da lista fechada: 422, nada é concatenado no SQL
curl -s "localhost:8080/api/relatorios/transacoes?ordenarPor=senha" -H "Authorization: Bearer $ADMIN"
```

---

## 9. Testes automatizados

Com o Oracle no ar (seção 2.2):

```bash
./gradlew :backend:test
```

`RotinasPlsqlTest` exercita os packages, as triggers e o Dynamic SQL pela mesma
camada JDBC que a API usa: débito de saldo, recusa por saldo insuficiente,
conversão em impacto, processamento de cashback (inclusive item com erro),
auditoria, alerta automático, filtros dinâmicos e recusa de ordenação inválida.

Os testes usam `@Transactional`, então tudo o que gravam é desfeito ao final — os
dados de exemplo permanecem intactos.

No GitHub, o pipeline `.github/workflows/ci-cd.yml` roda esses mesmos testes a cada
push, contra um Oracle criado só para a execução. Para ver o resultado: aba
**Actions** do repositório → clique na execução → role até o resumo.

O **ensaio de deploy** (API em container ligada ao Oracle + smoke tests) roda no
merge para a `master` ou manualmente em **Actions → CI/CD Solidari → Run workflow**.
O ambiente é temporário e apagado ao fim. Publicar a imagem da API no GitHub
Container Registry só acontece se você marcar **publicar_imagem** nesse disparo
manual — detalhes e motivos em `docs/FASE7_ORACLE_PLSQL_CICD.md`, seção 5.3.

---

## 10. Recriar o banco do zero

A qualquer momento, apague o schema e suba a API de novo. O Flyway refaz tudo:

```bash
docker compose down -v        # apaga o container e o volume do banco
docker compose up -d oracle
./gradlew :backend:bootRun
```

A massa de dados é gerada de forma determinística: qualquer máquina produz
exatamente os mesmos usuários, compras e doações.

---

## 11. Problemas comuns

**O Gradle falha logo no início, ou acusa versão de classe não suportada**
Está rodando sobre um JDK 24/25. Exporte o `JAVA_HOME` conforme a seção 2.1.

**`IO Error: The Network Adapter could not establish the connection`**
O Oracle não está no ar ou ainda está iniciando. Confira com `docker compose ps`
(precisa estar `healthy`) e, se usar outra porta, informe via `SOLIDARI_DB_PORT`.

**`ORA-01017: invalid username/password`**
Usuário ou senha do banco errados. No Docker são `solidari` / `solidari`; no Oracle
da FIAP, o seu RM e a sua senha (seção 2.2).

**`ORA-12514: listener does not currently know of service`**
O nome do serviço está errado. No Docker é `FREEPDB1`; no Oracle da FIAP, use o
serviço indicado no material da disciplina. Informe via `SOLIDARI_DB_SERVICE`.

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
