package com.alexandre.Barbearia_Api.dto.caixa.fechamento;

import com.alexandre.Barbearia_Api.model.CaixaFechamentoPeriodo;

import java.time.LocalDate;

public record CaixaFechamentoPreviewDTO(
        CaixaFechamentoPeriodo periodo,
        LocalDate referencia,
        CaixaFechamentoResumoDTO resumo,
        String nfceInfo
) {}
