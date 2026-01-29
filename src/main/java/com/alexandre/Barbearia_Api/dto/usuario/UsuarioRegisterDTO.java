package com.alexandre.Barbearia_Api.dto.usuario;

import com.alexandre.Barbearia_Api.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRegisterDTO(
        @NotBlank
        String name,
        @NotBlank
        String password,
        @NotNull
        UserRole role
) {}
