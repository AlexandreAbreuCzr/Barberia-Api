package com.alexandre.Barbearia_Api.dto.caixa.fechamento;

import com.alexandre.Barbearia_Api.model.CaixaFechamentoPeriodo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CaixaFechamentoCreateDTO(
        @NotNull CaixaFechamentoPeriodo periodo,
        LocalDate referencia,
        LocalDate inicio,
        LocalDate fim,
        BigDecimal saldoInformado,
        @Size(max = 500) String observacao,
        Boolean solicitarNfce
) {}
