package com.alexandre.Barbearia_Api.dto.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoCreateDTO(
        @NotBlank
        String name,
        @NotNull
        BigDecimal price,
        @NotNull
        Integer duracaoEmMinutos,
        @NotNull
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "100.0")
        BigDecimal percentualComissao
){}
