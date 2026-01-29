package com.alexandre.Barbearia_Api.dto.usuario.update;

import com.alexandre.Barbearia_Api.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRoleDTO(
        @NotNull
        UserRole role
) {}
