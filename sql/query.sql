-- ============================================================
--  SISTEMA DE DELIVERY — Schema PostgreSQL
--  Executar na ordem abaixo (respeita as FKs)
-- ============================================================

-- Extensão para UUID (opcional, mas útil no futuro)
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- 1. Restaurante
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurante (
    id        SERIAL      PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    endereco  VARCHAR(255) NOT NULL,
    telefone  VARCHAR(20)  NOT NULL,
    categoria VARCHAR(60)  NOT NULL
);


-- ------------------------------------------------------------
-- 2. Produto
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produto (
    id             SERIAL         PRIMARY KEY,
    restaurante_id INTEGER        NOT NULL REFERENCES restaurante(id) ON DELETE CASCADE,
    nome           VARCHAR(120)   NOT NULL,
    descricao      TEXT,
    preco          NUMERIC(10, 2) NOT NULL CHECK (preco >= 0)
);

-- ------------------------------------------------------------
-- 3. Cliente
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id        SERIAL       PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    email     VARCHAR(120) NOT NULL UNIQUE,
    telefone  VARCHAR(20)  NOT NULL,
    endereco  VARCHAR(255) NOT NULL
);

-- ------------------------------------------------------------
-- 4. Entregador
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS entregador (
    id       SERIAL      PRIMARY KEY,
    nome     VARCHAR(120) NOT NULL,
    telefone VARCHAR(20)  NOT NULL,
    veiculo  VARCHAR(60)  NOT NULL,
    status   VARCHAR(20)  NOT NULL DEFAULT 'DISPONIVEL'
                CHECK (status IN ('DISPONIVEL', 'EM_ENTREGA', 'INATIVO'))
);

-- ------------------------------------------------------------
-- 5. Pedido
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedido (
    id             SERIAL         PRIMARY KEY,
    cliente_id     INTEGER        NOT NULL REFERENCES cliente(id),
    restaurante_id INTEGER        NOT NULL REFERENCES restaurante(id),
    entregador_id  INTEGER                 REFERENCES entregador(id),   -- nullable
    status         VARCHAR(20)    NOT NULL DEFAULT 'AGUARDANDO'
                       CHECK (status IN ('AGUARDANDO','CONFIRMADO','EM_PREPARO',
                                         'SAIU_ENTREGA','ENTREGUE','CANCELADO')),
    subtotal       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    desconto       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    taxa_entrega   NUMERIC(10, 2) NOT NULL DEFAULT 8.00,
    valor_final    NUMERIC(10, 2) NOT NULL DEFAULT 0,
    data_criacao   TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- 6. Item de Pedido
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_pedido (
    id              SERIAL         PRIMARY KEY,
    pedido_id       INTEGER        NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    produto_id      INTEGER        NOT NULL REFERENCES produto(id),
    quantidade      INTEGER        NOT NULL CHECK (quantidade > 0),
    preco_unitario  NUMERIC(10, 2) NOT NULL CHECK (preco_unitario >= 0)
);

-- ------------------------------------------------------------
-- Índices úteis para as queries mais comuns
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_produto_restaurante   ON produto(restaurante_id);
CREATE INDEX IF NOT EXISTS idx_pedido_cliente        ON pedido(cliente_id);
CREATE INDEX IF NOT EXISTS idx_pedido_restaurante    ON pedido(restaurante_id);
CREATE INDEX IF NOT EXISTS idx_item_pedido_pedido    ON item_pedido(pedido_id);
CREATE INDEX IF NOT EXISTS idx_entregador_status     ON entregador(status);