package com.alexandre.Barbearia_Api.dto.servico.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ServicoUpdateDTO(
        String name,
        BigDecimal price,
        Integer duracaoEmMinutos,
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "100.0")
        BigDecimal percentualComissao,
        Boolean status
) {}

