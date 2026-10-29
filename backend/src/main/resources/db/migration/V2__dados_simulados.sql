-- =============================================================================
-- Solidari — carga de dados simulados (Oracle)
--
-- Os valores "aleatorios" vem de ORA_HASH(id_do_usuario, indice), nao de
-- DBMS_RANDOM. Isso mantem a massa identica em qualquer maquina: a demonstracao
-- nao muda a cada recriacao do banco.
--
-- As sequencias de 1..N (generate_series no PostgreSQL) sao geradas com
-- CONNECT BY LEVEL a partir da DUAL.
--
-- Senhas (hash BCrypt, custo 10):
--   admin@solidari.com -> admin123
--   demais usuarios    -> senha123
--
-- Observacao: as URLs de imagem tem '&'. Se for rodar este script manualmente
-- no SQL Developer, execute antes SET DEFINE OFF para o '&' nao ser tratado
-- como variavel de substituicao. Pelo Flyway nao e necessario.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Parceiros
-- -----------------------------------------------------------------------------
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Green Leaf Cafe',      'Refeições sustentáveis e cafés especiais',      'Meio Ambiente', '15% CASHBACK', 15.00, 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&q=80', 1,  -23.561414, -46.655881);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Vitality Gym',         'Saúde e bem-estar com plano solidário',        'Saúde',         '10% OFF',      10.00, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&q=80', 0, -23.570000, -46.640000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Eco Threads',          'Moda consciente e tecidos reciclados',         'Comunidade',    'SOLIDARITY+',   8.00, 'https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=800&q=80', 0, -23.550520, -46.633308);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Horta Urbana Vila Ana','Hortifrúti orgânico de produtores locais',     'Meio Ambiente', '12% CASHBACK', 12.00, 'https://images.unsplash.com/photo-1518843875459-f738682238a6?w=800&q=80', 1,  -23.545000, -46.640000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Livraria Semente',     'Livros novos e usados, clube de leitura',      'Educação',      '7% CASHBACK',   7.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Greenlight_Bookstore_interior_2026.jpg/960px-Greenlight_Bookstore_interior_2026.jpg', 0, -23.533773, -46.625290);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Padaria do Bairro',    'Panificadora de bairro com pão do dia',        'Comunidade',    '5% CASHBACK',   5.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/Moscow._Bakery_%22Daily_Bread%221.jpg/960px-Moscow._Bakery_%22Daily_Bread%221.jpg', 0, -23.558000, -46.662000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Bike Repair Co.',      'Manutenção de bicicletas e mobilidade limpa',  'Meio Ambiente', '10% CASHBACK', 10.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Bike_workshop_-_bicycle_repair_shop.jpg/960px-Bike_workshop_-_bicycle_repair_shop.jpg', 0, -23.564000, -46.652000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Clínica Bem Viver',    'Consultas populares e exames de rotina',       'Saúde',         '6% CASHBACK',   6.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/A_waiting_room_at_a_medical_healthcare_clinic%2C_doctor%27s_office%2C_hospital.jpg/960px-A_waiting_room_at_a_medical_healthcare_clinic%2C_doctor%27s_office%2C_hospital.jpg', 0, -23.575000, -46.645000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Estúdio Raiz',         'Oficinas de arte e cultura periférica',        'Cultura',       'SOLIDARITY+',   9.00, 'https://images.unsplash.com/photo-1521017432531-fbd92d768814?w=800&q=80', 1,  -23.540000, -46.630000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Mercado Justo',        'Mercearia de comércio justo',                  'Comunidade',    '11% CASHBACK', 11.00, 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&q=80', 0, -23.552000, -46.648000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Cursinho Aurora',      'Preparatório comunitário para vestibular',     'Educação',      '20% CASHBACK', 20.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c2/Professor_and_students_in_a_university_classroom_in_Tennessee.jpg/960px-Professor_and_students_in_a_university_classroom_in_Tennessee.jpg', 1,  -23.537000, -46.620000);
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude)
VALUES ('Pet Solidário',        'Petshop que apoia ONGs de resgate animal',     'Comunidade',    '8% CASHBACK',   8.00, 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=800&q=80', 0, -23.568000, -46.658000);

-- -----------------------------------------------------------------------------
-- Usuarios
-- -----------------------------------------------------------------------------
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Admin Solidari',    'admin@solidari.com',    '$2a$10$9.EARGxgU5K26whd3JR2AO22zI6fsh75yVraW3NZ4U2cNhy0XcPuS', 'ADMIN',   LOCALTIMESTAMP - NUMTODSINTERVAL(300, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Ana Beatriz Rocha', 'ana.rocha@email.com',   '$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(250, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Bruno Martins',     'bruno.martins@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(240, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Carla Nogueira',    'carla.nogueira@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(230, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Diego Almeida',     'diego.almeida@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(220, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Eduarda Lima',      'eduarda.lima@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(210, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Felipe Souza',      'felipe.souza@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(200, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Gabriela Pinto',    'gabriela.pinto@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(190, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Henrique Dias',     'henrique.dias@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(180, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Isabela Moreira',   'isabela.moreira@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(170, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('João Pedro Castro', 'joao.castro@email.com', '$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(160, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Larissa Campos',    'larissa.campos@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(150, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Marcos Vinícius',   'marcos.vinicius@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO',LOCALTIMESTAMP - NUMTODSINTERVAL(140, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Natália Ferraz',    'natalia.ferraz@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(120, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Otávio Ramos',      'otavio.ramos@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', LOCALTIMESTAMP - NUMTODSINTERVAL(100, 'DAY'));
INSERT INTO usuarios (nome, email, senha, papel, criado_em)
VALUES ('Priscila Tavares',  'priscila.tavares@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO',LOCALTIMESTAMP - NUMTODSINTERVAL(80, 'DAY'));

-- -----------------------------------------------------------------------------
-- Transacoes: consumo dos usuarios nos parceiros.
--
-- Transacoes com mais de 15 dias entram como PROCESSADA (cashback ja apurado);
-- as recentes ficam PENDENTE, para que pkg_cashback.sp_processar_cashback tenha
-- o que processar durante a demonstracao.
-- -----------------------------------------------------------------------------
INSERT INTO transacoes (usuario_id, parceiro_id, valor, cashback_gerado, status, data, processada_em)
WITH numeros AS (
    SELECT LEVEL AS i FROM dual CONNECT BY LEVEL <= 15
),
gerado AS (
    -- Entre 8 e 15 compras por usuario, quantidade derivada do proprio id.
    SELECT u.id AS usuario_id,
           ROUND(20 + MOD(ORA_HASH(u.id || ':' || n.i || ':valor'), 38000) / 100, 2) AS valor,
           MOD(ORA_HASH(u.id || ':' || n.i || ':dias'), 181)                          AS dias,
           MOD(ORA_HASH(u.id || ':' || n.i || ':parc'), 12) + 1                       AS parceiro_rn
    FROM usuarios u
    JOIN numeros n ON n.i <= 8 + MOD(u.id, 8)
    WHERE u.papel = 'USUARIO'
),
parc AS (
    SELECT id, percentual_cashback, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM parceiros
)
SELECT g.usuario_id,
       p.id,
       g.valor,
       CASE WHEN g.dias > 15 THEN ROUND(g.valor * p.percentual_cashback / 100, 2) END,
       CASE WHEN g.dias > 15 THEN 'PROCESSADA' ELSE 'PENDENTE' END,
       LOCALTIMESTAMP - NUMTODSINTERVAL(g.dias, 'DAY'),
       CASE WHEN g.dias > 15 THEN LOCALTIMESTAMP - NUMTODSINTERVAL(g.dias - 1, 'DAY') END
FROM gerado g
JOIN parc p ON p.rn = g.parceiro_rn;

-- -----------------------------------------------------------------------------
-- Doacoes: cada usuario doa ~55% do credito que ja acumulou, parcelado.
-- O teto de 55% garante que o saldo final nunca fique negativo.
-- -----------------------------------------------------------------------------
INSERT INTO doacoes (usuario_id, parceiro_id, valor, categoria_impacto, data)
WITH plano AS (
    SELECT t.usuario_id,
           SUM(t.cashback_gerado)    AS total,
           3 + MOD(t.usuario_id, 5)  AS qtd
    FROM transacoes t
    WHERE t.status = 'PROCESSADA'
    GROUP BY t.usuario_id
    HAVING SUM(t.cashback_gerado) >= 50
),
numeros AS (
    SELECT LEVEL AS i FROM dual CONNECT BY LEVEL <= 7
),
parc AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM parceiros
)
SELECT pl.usuario_id,
       p.id,
       ROUND(pl.total * 0.55 / pl.qtd, 2),
       CASE WHEN MOD(ORA_HASH(pl.usuario_id || ':' || n.i || ':cat'), 2) = 0
            THEN 'RESTAURACAO_AMBIENTAL' ELSE 'BEM_ESTAR_COMUNITARIO' END,
       LOCALTIMESTAMP - NUMTODSINTERVAL(MOD(ORA_HASH(pl.usuario_id || ':' || n.i || ':dia'), 121), 'DAY')
FROM plano pl
JOIN numeros n ON n.i <= pl.qtd
JOIN parc p ON p.rn = MOD(ORA_HASH(pl.usuario_id || ':' || n.i || ':parc'), 12) + 1;

-- -----------------------------------------------------------------------------
-- Saldo: credito processado menos o que ja foi doado.
-- -----------------------------------------------------------------------------
UPDATE usuarios u
SET saldo = NVL((SELECT SUM(t.cashback_gerado) FROM transacoes t
                 WHERE t.usuario_id = u.id AND t.status = 'PROCESSADA'), 0)
          - NVL((SELECT SUM(d.valor) FROM doacoes d
                 WHERE d.usuario_id = u.id), 0);
