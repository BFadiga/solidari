-- =============================================================================
-- Solidari — carga de dados simulados
--
-- Os valores "aleatorios" vem de md5(id_do_usuario, indice), nao de random().
-- Isso mantem a massa identica em qualquer maquina — a demonstracao nao muda a
-- cada recriacao do banco — e, ao contrario de random() dentro de LATERAL, varia
-- de fato por usuario (o planejador avalia random() uma unica vez e reaproveita).
--
-- Senhas (hash BCrypt, custo 10):
--   admin@solidari.com -> admin123
--   demais usuarios    -> senha123
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Parceiros
-- -----------------------------------------------------------------------------
INSERT INTO parceiros (nome, descricao, categoria, badge, percentual_cashback, imagem_url, destaque, latitude, longitude) VALUES
('Green Leaf Cafe',      'Refeições sustentáveis e cafés especiais',      'Meio Ambiente', '15% CASHBACK', 15.00, 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&q=80', TRUE,  -23.561414, -46.655881),
('Vitality Gym',         'Saúde e bem-estar com plano solidário',        'Saúde',         '10% OFF',      10.00, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&q=80', FALSE, -23.570000, -46.640000),
('Eco Threads',          'Moda consciente e tecidos reciclados',         'Comunidade',    'SOLIDARITY+',   8.00, 'https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=800&q=80', FALSE, -23.550520, -46.633308),
('Horta Urbana Vila Ana','Hortifrúti orgânico de produtores locais',     'Meio Ambiente', '12% CASHBACK', 12.00, 'https://images.unsplash.com/photo-1518843875459-f738682238a6?w=800&q=80', TRUE,  -23.545000, -46.640000),
('Livraria Semente',     'Livros novos e usados, clube de leitura',      'Educação',      '7% CASHBACK',   7.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Greenlight_Bookstore_interior_2026.jpg/960px-Greenlight_Bookstore_interior_2026.jpg', FALSE, -23.533773, -46.625290),
('Padaria do Bairro',    'Panificadora de bairro com pão do dia',        'Comunidade',    '5% CASHBACK',   5.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/Moscow._Bakery_%22Daily_Bread%221.jpg/960px-Moscow._Bakery_%22Daily_Bread%221.jpg', FALSE, -23.558000, -46.662000),
('Bike Repair Co.',      'Manutenção de bicicletas e mobilidade limpa',  'Meio Ambiente', '10% CASHBACK', 10.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Bike_workshop_-_bicycle_repair_shop.jpg/960px-Bike_workshop_-_bicycle_repair_shop.jpg', FALSE, -23.564000, -46.652000),
('Clínica Bem Viver',    'Consultas populares e exames de rotina',       'Saúde',         '6% CASHBACK',   6.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/A_waiting_room_at_a_medical_healthcare_clinic%2C_doctor%27s_office%2C_hospital.jpg/960px-A_waiting_room_at_a_medical_healthcare_clinic%2C_doctor%27s_office%2C_hospital.jpg', FALSE, -23.575000, -46.645000),
('Estúdio Raiz',         'Oficinas de arte e cultura periférica',        'Cultura',       'SOLIDARITY+',   9.00, 'https://images.unsplash.com/photo-1521017432531-fbd92d768814?w=800&q=80', TRUE,  -23.540000, -46.630000),
('Mercado Justo',        'Mercearia de comércio justo',                  'Comunidade',    '11% CASHBACK', 11.00, 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&q=80', FALSE, -23.552000, -46.648000),
('Cursinho Aurora',      'Preparatório comunitário para vestibular',     'Educação',      '20% CASHBACK', 20.00, 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c2/Professor_and_students_in_a_university_classroom_in_Tennessee.jpg/960px-Professor_and_students_in_a_university_classroom_in_Tennessee.jpg', TRUE,  -23.537000, -46.620000),
('Pet Solidário',        'Petshop que apoia ONGs de resgate animal',     'Comunidade',    '8% CASHBACK',   8.00, 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=800&q=80', FALSE, -23.568000, -46.658000);

-- -----------------------------------------------------------------------------
-- Usuarios
-- -----------------------------------------------------------------------------
INSERT INTO usuarios (nome, email, senha, papel, criado_em) VALUES
('Admin Solidari',    'admin@solidari.com',    '$2a$10$9.EARGxgU5K26whd3JR2AO22zI6fsh75yVraW3NZ4U2cNhy0XcPuS', 'ADMIN',   now() - INTERVAL '300 days'),
('Ana Beatriz Rocha', 'ana.rocha@email.com',   '$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', now() - INTERVAL '250 days'),
('Bruno Martins',     'bruno.martins@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', now() - INTERVAL '240 days'),
('Carla Nogueira',    'carla.nogueira@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', now() - INTERVAL '230 days'),
('Diego Almeida',     'diego.almeida@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', now() - INTERVAL '220 days'),
('Eduarda Lima',      'eduarda.lima@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', now() - INTERVAL '210 days'),
('Felipe Souza',      'felipe.souza@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', now() - INTERVAL '200 days'),
('Gabriela Pinto',    'gabriela.pinto@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', now() - INTERVAL '190 days'),
('Henrique Dias',     'henrique.dias@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq', 'USUARIO', now() - INTERVAL '180 days'),
('Isabela Moreira',   'isabela.moreira@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', now() - INTERVAL '170 days'),
('João Pedro Castro', 'joao.castro@email.com', '$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', now() - INTERVAL '160 days'),
('Larissa Campos',    'larissa.campos@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', now() - INTERVAL '150 days'),
('Marcos Vinícius',   'marcos.vinicius@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO',now() - INTERVAL '140 days'),
('Natália Ferraz',    'natalia.ferraz@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO', now() - INTERVAL '120 days'),
('Otávio Ramos',      'otavio.ramos@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq',  'USUARIO', now() - INTERVAL '100 days'),
('Priscila Tavares',  'priscila.tavares@email.com','$2a$10$0QYNY/gyvYl5TiipZWQDCe1a9qu7E9oz7COKm9Tk54aNixkwLKJdq','USUARIO',now() - INTERVAL '80 days');

-- -----------------------------------------------------------------------------
-- Transacoes: consumo dos usuarios nos parceiros.
--
-- Transacoes com mais de 15 dias entram como PROCESSADA (cashback ja apurado);
-- as recentes ficam PENDENTE, para que sp_processar_cashback tenha o que
-- processar durante a demonstracao.
-- -----------------------------------------------------------------------------
INSERT INTO transacoes (usuario_id, parceiro_id, valor, cashback_gerado, status, data, processada_em)
SELECT
    u.id,
    p.id,
    g.valor,
    CASE WHEN g.dias > 15 THEN ROUND(g.valor * p.percentual_cashback / 100, 2) END,
    CASE WHEN g.dias > 15 THEN 'PROCESSADA' ELSE 'PENDENTE' END,
    now() - (g.dias || ' days')::INTERVAL,
    CASE WHEN g.dias > 15 THEN now() - ((g.dias - 1) || ' days')::INTERVAL END
FROM usuarios u
-- Entre 8 e 15 compras por usuario, quantidade derivada do proprio id.
CROSS JOIN LATERAL generate_series(1, 8 + (u.id % 8)::INT) AS s(i)
CROSS JOIN LATERAL (
    SELECT
        -- 7 digitos hex -> bit(28) -> inteiro sempre positivo (nao estoura em abs()).
        ROUND(20 + (('x' || substr(md5(u.id || ':' || s.i || ':valor'), 1, 7))::BIT(28)::INT % 38000)::NUMERIC / 100, 2) AS valor,
        (('x' || substr(md5(u.id || ':' || s.i || ':dias'),  1, 7))::BIT(28)::INT % 181)     AS dias,
        (('x' || substr(md5(u.id || ':' || s.i || ':parc'),  1, 7))::BIT(28)::INT % 12) + 1  AS parceiro_rn
) g
JOIN (SELECT id, percentual_cashback, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM parceiros) p
  ON p.rn = g.parceiro_rn
WHERE u.papel = 'USUARIO';

-- -----------------------------------------------------------------------------
-- Doacoes: cada usuario doa ~55% do credito que ja acumulou, parcelado.
-- O teto de 55% garante que o saldo final nunca fique negativo.
-- -----------------------------------------------------------------------------
INSERT INTO doacoes (usuario_id, parceiro_id, valor, categoria_impacto, data)
SELECT
    pl.usuario_id,
    p.id,
    ROUND(pl.total * 0.55 / pl.qtd, 2),
    CASE WHEN (('x' || substr(md5(pl.usuario_id || ':' || s.i || ':cat'), 1, 7))::BIT(28)::INT % 2) = 0
         THEN 'RESTAURACAO_AMBIENTAL' ELSE 'BEM_ESTAR_COMUNITARIO' END,
    now() - ((('x' || substr(md5(pl.usuario_id || ':' || s.i || ':dia'), 1, 7))::BIT(28)::INT % 121) || ' days')::INTERVAL
FROM (
    SELECT
        t.usuario_id,
        SUM(t.cashback_gerado)      AS total,
        3 + (t.usuario_id % 5)::INT AS qtd
    FROM transacoes t
    WHERE t.status = 'PROCESSADA'
    GROUP BY t.usuario_id
    HAVING SUM(t.cashback_gerado) >= 50
) pl
CROSS JOIN LATERAL generate_series(1, pl.qtd) AS s(i)
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM parceiros) p
  ON p.rn = (('x' || substr(md5(pl.usuario_id || ':' || s.i || ':parc'), 1, 7))::BIT(28)::INT % 12) + 1;

-- -----------------------------------------------------------------------------
-- Saldo: credito processado menos o que ja foi doado.
-- -----------------------------------------------------------------------------
UPDATE usuarios u
SET saldo = COALESCE((SELECT SUM(t.cashback_gerado) FROM transacoes t
                      WHERE t.usuario_id = u.id AND t.status = 'PROCESSADA'), 0)
          - COALESCE((SELECT SUM(d.valor) FROM doacoes d
                      WHERE d.usuario_id = u.id), 0);
