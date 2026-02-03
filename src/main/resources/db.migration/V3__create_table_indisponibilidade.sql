-- =========================
-- ENUMS
-- =========================
-- Ajuste os valores para bater com o seu enum TipoIndisponibilidade no Java.
-- Exemplo comum: FERIAS, FOLGA, MANUTENCAO, OUTRO
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_indisponibilidade') THEN
        CREATE TYPE tipo_indisponibilidade AS ENUM (
            'FERIAS',
            'FOLGA',
            'MANUTENCAO',
            'OUTRO'
        );
    END IF;
END$$;

-- =========================
-- TABELA: indisponibilidade
-- =========================
CREATE TABLE IF NOT EXISTS indisponibilidade (
    id BIGSERIAL PRIMARY KEY,

    barbeiro_id UUID NOT NULL,

    tipo tipo_indisponibilidade NOT NULL,

    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,

    data_de_modificacao TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_indisponibilidade_barbeiro
        FOREIGN KEY (barbeiro_id)
        REFERENCES usuario (id),

    -- Garante intervalo válido
    CONSTRAINT ck_indisponibilidade_intervalo_valido
        CHECK (inicio < fim)
);

-- =========================
-- ÍNDICES
-- =========================

-- Consultas típicas: listar por barbeiro e período / checar conflito
CREATE INDEX IF NOT EXISTS idx_indisponibilidade_barbeiro
ON indisponibilidade (barbeiro_id);

CREATE INDEX IF NOT EXISTS idx_indisponibilidade_periodo
ON indisponibilidade (inicio, fim);

-- Opcional (recomendado): acelera busca de conflitos por barbeiro+período
CREATE INDEX IF NOT EXISTS idx_indisponibilidade_barbeiro_periodo
ON indisponibilidade (barbeiro_id, inicio, fim);

-- =========================
-- TRIGGERS
-- =========================
-- Reaproveita sua função set_data_de_modificacao() já existente
-- (se ela ainda não existir nesta migration, crie antes como no seu padrão)

CREATE TRIGGER trg_indisponibilidade_data_modificacao
BEFORE UPDATE ON indisponibilidade
FOR EACH ROW
EXECUTE FUNCTION set_data_de_modificacao();
