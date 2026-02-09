package com.alexandre.Barbearia_Api.dto.caixa.fechamento;

import com.alexandre.Barbearia_Api.model.CaixaFechamentoNfceStatus;
import com.alexandre.Barbearia_Api.model.CaixaFechamentoPeriodo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CaixaFechamentoResponseDTO(
        Long id,
        CaixaFechamentoPeriodo periodo,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoApurado,
        BigDecimal saldoInformado,
        BigDecimal diferenca,
        Long totalLancamentos,
        String observacao,
        Boolean solicitarNfce,
        CaixaFechamentoNfceStatus nfceStatus,
        String nfceChave,
        String fechadoPorUsername,
        LocalDateTime dataDeCriacao
) {}
