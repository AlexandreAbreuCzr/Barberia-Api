package com.alexandre.Barbearia_Api.dto.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoCreateDTO(
        @NotBlank String nome,
        @NotNull @Min(1) @Max(5) Integer nota,
        @NotBlank String comentario
) {}
