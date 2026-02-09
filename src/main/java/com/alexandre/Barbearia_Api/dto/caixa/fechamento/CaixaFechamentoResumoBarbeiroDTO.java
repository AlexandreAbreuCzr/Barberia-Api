package com.alexandre.Barbearia_Api.dto.caixa.fechamento;

import java.math.BigDecimal;

public record CaixaFechamentoResumoBarbeiroDTO(
        String barbeiroUsername,
        BigDecimal entradas,
        BigDecimal saidas,
        BigDecimal saldo,
        Long totalLancamentos
) {}
