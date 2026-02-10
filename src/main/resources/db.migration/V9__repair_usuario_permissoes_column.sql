ALTER TABLE usuario
ADD COLUMN IF NOT EXISTS permissoes TEXT;

UPDATE usuario
SET permissoes = ''
WHERE permissoes IS NULL;

ALTER TABLE usuario
ALTER COLUMN permissoes SET DEFAULT '';

ALTER TABLE usuario
ALTER COLUMN permissoes SET NOT NULL;
