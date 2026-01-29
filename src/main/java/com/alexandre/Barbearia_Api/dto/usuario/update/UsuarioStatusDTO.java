package com.alexandre.Barbearia_Api.dto.usuario.update;

import jakarta.validation.constraints.NotNull;

public record UsuarioStatusDTO(
        @NotNull
        boolean status
) {}
