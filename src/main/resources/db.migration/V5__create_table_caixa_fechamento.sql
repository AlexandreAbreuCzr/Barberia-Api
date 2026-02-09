CREATE TABLE IF NOT EXISTS caixa_fechamento (
    id BIGSERIAL PRIMARY KEY,
    periodo VARCHAR(30) NOT NULL,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    total_entradas NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_saidas NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_apurado NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_informado NUMERIC(12,2),
    diferenca NUMERIC(12,2),
    total_lancamentos BIGINT NOT NULL DEFAULT 0,
    observacao VARCHAR(500),
    solicitar_nfce BOOLEAN NOT NULL DEFAULT false,
    nfce_status VARCHAR(30) NOT NULL DEFAULT 'NAO_SOLICITADA',
    nfce_chave VARCHAR(64),
    fechado_por_id UUID,
    data_de_criacao TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_caixa_fechamento_fechado_por
        FOREIGN KEY (fechado_por_id)
        REFERENCES usuario (id)
);

CREATE INDEX IF NOT EXISTS idx_caixa_fechamento_periodo
ON caixa_fechamento (periodo);

CREATE INDEX IF NOT EXISTS idx_caixa_fechamento_intervalo
ON caixa_fechamento (data_inicio, data_fim);

CREATE INDEX IF NOT EXISTS idx_caixa_fechamento_data_criacao
ON caixa_fechamento (data_de_criacao);

CREATE UNIQUE INDEX IF NOT EXISTS uk_caixa_fechamento_periodo_intervalo
ON caixa_fechamento (periodo, data_inicio, data_fim);
