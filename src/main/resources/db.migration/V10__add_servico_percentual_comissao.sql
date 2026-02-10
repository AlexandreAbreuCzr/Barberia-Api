ALTER TABLE servico
ADD COLUMN IF NOT EXISTS percentual_comissao NUMERIC(5,2);

UPDATE servico
SET percentual_comissao = 50.00
WHERE percentual_comissao IS NULL;

ALTER TABLE servico
ALTER COLUMN percentual_comissao SET DEFAULT 50.00;

ALTER TABLE servico
ALTER COLUMN percentual_comissao SET NOT NULL;
