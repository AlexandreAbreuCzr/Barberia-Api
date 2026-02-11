DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'servico'
          AND column_name = 'percentual_comissao'
    ) THEN
        UPDATE servico
        SET percentual_comissao = 50.00
        WHERE percentual_comissao IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'usuario'
          AND column_name = 'permissoes'
    ) THEN
        UPDATE usuario
        SET permissoes = ''
        WHERE permissoes IS NULL;
    END IF;
END
$$;
