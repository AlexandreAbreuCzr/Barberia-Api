package com.alexandre.Barbearia_Api.dto.indisponibilidade;

import com.alexandre.Barbearia_Api.model.TipoIndisponibilidade;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public record IndisponibilidadeCreateDTO(
        @NotNull String barbeiroUsername,
        @NotNull TipoIndisponibilidade tipo,
        @NotNull LocalDateTime inicio,
        @NotNull LocalDateTime fim
){}