package com.alexandre.Barbearia_Api.dto.servico.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ServicoUpdateDTO(
        String name,
        BigDecimal price,
        Integer duracaoEmMinutos,
        Boolean status
) {}

