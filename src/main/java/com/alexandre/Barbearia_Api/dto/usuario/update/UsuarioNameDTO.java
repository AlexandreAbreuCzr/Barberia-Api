package com.alexandre.Barbearia_Api.dto.usuario.update;

import com.alexandre.Barbearia_Api.model.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioNameDTO(
        @NotEmpty
        @Size(max = 255)
        String name

) {}
