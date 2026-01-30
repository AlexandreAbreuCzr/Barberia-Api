-- =========================
-- V2 - ALTERAÇÕES USUÁRIO
-- =========================

-- 1) Adiciona novas colunas (sem restrições inicialmente)
ALTER TABLE usuario
ADD COLUMN username VARCHAR(50),
ADD COLUMN email VARCHAR(255),
ADD COLUMN telefone VARCHAR(20);

-- 2) Preenche valores temporários de forma SEGURA
-- username: lowercase, sem espaços, único
UPDATE usuario
SET username = lower(regexp_replace(name, '\s+', '_', 'g')) || '_' || id
WHERE username IS NULL;

-- email: único e válido o suficiente para DEV
UPDATE usuario
SET email = lower(regexp_replace(name, '\s+', '', 'g')) || id || '@example.com'
WHERE email IS NULL;

-- 3) Regras de obrigatoriedade
ALTER TABLE usuario
ALTER COLUMN username SET NOT NULL,
ALTER COLUMN email SET NOT NULL;

-- 4) CHECK - username
-- apenas letras, números, ponto e underline
ALTER TABLE usuario
ADD CONSTRAINT ck_usuario_username_format
CHECK (username ~ '^[a-z0-9._]+$');

-- 5) CHECK - email
-- regra simples, mas decente
ALTER TABLE usuario
ADD CONSTRAINT ck_usuario_email_format
CHECK (email ~ '^[^@]+@[^@]+\.[^@]+$');

-- 6) CHECK - telefone (opcional)
-- apenas números, entre 10 e 15 dígitos
ALTER TABLE usuario
ADD CONSTRAINT ck_usuario_telefone_format
CHECK (
    telefone IS NULL
    OR telefone ~ '^[0-9]{10,15}$'
);

-- 7) Índices únicos
CREATE UNIQUE INDEX idx_usuario_username
ON usuario (username);

CREATE UNIQUE INDEX idx_usuario_email
ON usuario (email);
