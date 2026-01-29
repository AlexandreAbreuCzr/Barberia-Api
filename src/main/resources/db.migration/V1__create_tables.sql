-- =========================
-- EXTENSÕES
-- =========================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================
-- ENUMS
-- =========================
CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'BARBEIRO',
    'USER'
);

CREATE TYPE agendamento_status AS ENUM (
    'REQUISITADO',
    'AGENDADO',
    'CANCELADO',
    'CONCLUIDO'
);

-- =========================
-- TABELA: usuario
-- =========================
CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,

    role user_role NOT NULL,

    status BOOLEAN NOT NULL DEFAULT true,

    data_de_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_de_modificacao TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================
-- TABELA: servico
-- =========================
CREATE TABLE servico (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    price NUMERIC(10,2) NOT NULL,

    duracao_media_em_minutos INTEGER NOT NULL,

    status BOOLEAN NOT NULL DEFAULT true,

    data_de_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_de_modificacao TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================
-- TABELA: agendamento
-- =========================

CREATE TABLE agendamento (
    id BIGSERIAL PRIMARY KEY,

    cliente_id UUID NOT NULL,
    barbeiro_id UUID NOT NULL,
    servico_id BIGINT NOT NULL,

    data DATE NOT NULL,
    hora TIME NOT NULL,

    status agendamento_status NOT NULL DEFAULT 'REQUISITADO',

    data_de_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_de_modificacao TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_agendamento_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES usuario (id),

    CONSTRAINT fk_agendamento_barbeiro
        FOREIGN KEY (barbeiro_id)
        REFERENCES usuario (id),

    CONSTRAINT fk_agendamento_servico
        FOREIGN KEY (servico_id)
        REFERENCES servico (id)
);



-- =========================
-- ÍNDICES
-- =========================

-- Usuário
CREATE UNIQUE INDEX idx_usuario_name
ON usuario (name);

CREATE INDEX idx_usuario_role
ON usuario (role);

-- Serviço
CREATE INDEX idx_servico_name
ON servico (name);

-- =========================
-- FUNÇÃO DE TRIGGER
-- =========================

CREATE OR REPLACE FUNCTION set_data_de_modificacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_de_modificacao = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =========================
-- TRIGGERS
-- =========================

-- Atualiza data_de_modificacao automaticamente

-- Usuario
CREATE TRIGGER trg_usuario_data_modificacao
BEFORE UPDATE ON usuario
FOR EACH ROW
EXECUTE FUNCTION set_data_de_modificacao();

-- Servico
CREATE TRIGGER trg_servico_data_modificacao
BEFORE UPDATE ON servico
FOR EACH ROW
EXECUTE FUNCTION set_data_de_modificacao();

-- Agendamento
CREATE TRIGGER trg_agendamento_data_modificacao
BEFORE UPDATE ON agendamento
FOR EACH ROW
EXECUTE FUNCTION set_data_de_modificacao();
