package com.alexandre.Barbearia_Api.dto.caixa.fechamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CaixaFechamentoResumoDTO(
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoApurado,
        Long totalLancamentos,
        List<CaixaFechamentoResumoBarbeiroDTO> porBarbeiro
) {}
