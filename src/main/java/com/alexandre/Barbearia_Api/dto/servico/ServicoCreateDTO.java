package com.alexandre.Barbearia_Api.dto.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoCreateDTO(
        @NotBlank
        String name,
        @NotNull
        BigDecimal price,
        @NotNull
        Integer duracaoEmMinutos
){}
