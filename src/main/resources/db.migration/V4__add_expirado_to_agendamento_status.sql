-- Adiciona novo valor ao ENUM (PostgreSQL)
ALTER TYPE agendamento_status ADD VALUE IF NOT EXISTS 'EXPIRADO';

-- Índice para acelerar expiração e filtros
CREATE INDEX IF NOT EXISTS idx_agendamento_status_data_hora
ON agendamento (status, data, hora);
