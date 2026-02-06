package com.alexandre.Barbearia_Api.dto.caixa;

import com.alexandre.Barbearia_Api.model.CaixaTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CaixaCreateDTO(
        @NotNull CaixaTipo tipo,
        @NotBlank String descricao,
        @NotNull BigDecimal valor,
        String barbeiroUsername
) {}
