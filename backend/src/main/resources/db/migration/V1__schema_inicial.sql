-- =============================================================================
-- Solidari — schema inicial
--
-- O banco passa a ser a fonte de verdade do schema: o Hibernate roda em
-- ddl-auto=validate e nao altera mais nada por conta propria.
--
-- Valores monetarios usam NUMERIC(10,2). Double acumula erro de arredondamento
-- e nao e tipo aceitavel para saldo/doacao.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- usuarios — cadastro e carteira de credito
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios (
    id          BIGSERIAL       PRIMARY KEY,
    nome        VARCHAR(120)    NOT NULL,
    email       VARCHAR(180)    NOT NULL,
    -- Hash BCrypt (60 caracteres). A senha em claro nunca e persistida.
    senha       VARCHAR(100)    NOT NULL,
    saldo       NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    papel       VARCHAR(20)     NOT NULL DEFAULT 'USUARIO',
    criado_em   TIMESTAMP       NOT NULL DEFAULT now(),

    CONSTRAINT uk_usuarios_email     UNIQUE (email),
    CONSTRAINT ck_usuarios_saldo     CHECK (saldo >= 0),
    CONSTRAINT ck_usuarios_papel     CHECK (papel IN ('USUARIO', 'ADMIN'))
);

COMMENT ON TABLE  usuarios       IS 'Usuarios do app Solidari, com saldo de cashback disponivel para doacao';
COMMENT ON COLUMN usuarios.senha IS 'Hash BCrypt da senha; nunca armazenar texto puro';
COMMENT ON COLUMN usuarios.saldo IS 'Credito de cashback acumulado e ainda nao doado, em reais';

-- -----------------------------------------------------------------------------
-- parceiros — estabelecimentos que geram cashback
-- -----------------------------------------------------------------------------
CREATE TABLE parceiros (
    id                   BIGSERIAL       PRIMARY KEY,
    nome                 VARCHAR(120)    NOT NULL,
    descricao            VARCHAR(255)    NOT NULL,
    categoria            VARCHAR(60)     NOT NULL,
    -- Rotulo exibido no app (ex.: '15% CASHBACK'). E texto de interface:
    -- o calculo usa percentual_cashback, nunca esta coluna.
    badge                VARCHAR(40)     NOT NULL,
    percentual_cashback  NUMERIC(5, 2)   NOT NULL DEFAULT 0.00,
    imagem_url           VARCHAR(500),
    destaque             BOOLEAN         NOT NULL DEFAULT FALSE,
    latitude             DOUBLE PRECISION,
    longitude            DOUBLE PRECISION,
    ativo                BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em            TIMESTAMP       NOT NULL DEFAULT now(),

    CONSTRAINT ck_parceiros_percentual CHECK (percentual_cashback >= 0 AND percentual_cashback <= 100),
    CONSTRAINT ck_parceiros_latitude   CHECK (latitude  IS NULL OR latitude  BETWEEN  -90 AND  90),
    CONSTRAINT ck_parceiros_longitude  CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

COMMENT ON TABLE  parceiros                     IS 'Estabelecimentos parceiros que devolvem parte do consumo como credito';
COMMENT ON COLUMN parceiros.percentual_cashback IS 'Percentual do consumo devolvido como credito (0 a 100)';

-- -----------------------------------------------------------------------------
-- transacoes — consumo do usuario no parceiro; origem do credito
-- -----------------------------------------------------------------------------
CREATE TABLE transacoes (
    id               BIGSERIAL       PRIMARY KEY,
    usuario_id       BIGINT          NOT NULL,
    parceiro_id      BIGINT          NOT NULL,
    valor            NUMERIC(10, 2)  NOT NULL,
    cashback_gerado  NUMERIC(10, 2),
    status           VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    erro             VARCHAR(400),
    data             TIMESTAMP       NOT NULL DEFAULT now(),
    processada_em    TIMESTAMP,

    CONSTRAINT fk_transacoes_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios  (id),
    CONSTRAINT fk_transacoes_parceiro FOREIGN KEY (parceiro_id) REFERENCES parceiros (id),
    CONSTRAINT ck_transacoes_valor    CHECK (valor > 0),
    CONSTRAINT ck_transacoes_status   CHECK (status IN ('PENDENTE', 'PROCESSADA', 'ERRO'))
);

COMMENT ON TABLE  transacoes                 IS 'Compras do usuario em parceiros; processadas em lote viram credito';
COMMENT ON COLUMN transacoes.cashback_gerado IS 'Credito apurado no processamento; nulo enquanto PENDENTE';
COMMENT ON COLUMN transacoes.status          IS 'PENDENTE ate o processamento; PROCESSADA ou ERRO depois';

-- -----------------------------------------------------------------------------
-- doacoes — conversao de credito em impacto social
-- -----------------------------------------------------------------------------
CREATE TABLE doacoes (
    id                 BIGSERIAL       PRIMARY KEY,
    usuario_id         BIGINT          NOT NULL,
    parceiro_id        BIGINT,
    valor              NUMERIC(10, 2)  NOT NULL,
    categoria_impacto  VARCHAR(40)     NOT NULL,
    data               TIMESTAMP       NOT NULL DEFAULT now(),

    CONSTRAINT fk_doacoes_usuario   FOREIGN KEY (usuario_id)  REFERENCES usuarios  (id),
    CONSTRAINT fk_doacoes_parceiro  FOREIGN KEY (parceiro_id) REFERENCES parceiros (id),
    CONSTRAINT ck_doacoes_valor     CHECK (valor > 0),
    CONSTRAINT ck_doacoes_categoria CHECK (categoria_impacto IN ('RESTAURACAO_AMBIENTAL', 'BEM_ESTAR_COMUNITARIO'))
);

COMMENT ON TABLE  doacoes                   IS 'Doacoes feitas com o credito de cashback do usuario';
COMMENT ON COLUMN doacoes.categoria_impacto IS 'Define a conversao em impacto: arvores plantadas ou refeicoes servidas';

-- -----------------------------------------------------------------------------
-- alertas — saida das rotinas automatizadas do banco
-- -----------------------------------------------------------------------------
CREATE TABLE alertas (
    id           BIGSERIAL     PRIMARY KEY,
    tipo         VARCHAR(40)   NOT NULL,
    severidade   VARCHAR(20)   NOT NULL DEFAULT 'INFO',
    descricao    VARCHAR(400)  NOT NULL,
    usuario_id   BIGINT,
    parceiro_id  BIGINT,
    resolvido    BOOLEAN       NOT NULL DEFAULT FALSE,
    criado_em    TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT fk_alertas_usuario    FOREIGN KEY (usuario_id)  REFERENCES usuarios  (id),
    CONSTRAINT fk_alertas_parceiro   FOREIGN KEY (parceiro_id) REFERENCES parceiros (id),
    CONSTRAINT ck_alertas_severidade CHECK (severidade IN ('INFO', 'ATENCAO', 'CRITICO'))
);

COMMENT ON TABLE alertas IS 'Alertas gerados pela procedure sp_gerar_alertas, consumidos pelo dashboard';

-- -----------------------------------------------------------------------------
-- Indices de apoio as consultas mais frequentes
-- -----------------------------------------------------------------------------
CREATE INDEX idx_doacoes_usuario_data   ON doacoes    (usuario_id, data DESC);
CREATE INDEX idx_doacoes_categoria      ON doacoes    (categoria_impacto);
CREATE INDEX idx_transacoes_status      ON transacoes (status);
CREATE INDEX idx_transacoes_usuario     ON transacoes (usuario_id);
CREATE INDEX idx_parceiros_categoria    ON parceiros  (categoria);
CREATE INDEX idx_alertas_resolvido      ON alertas    (resolvido, criado_em DESC);
