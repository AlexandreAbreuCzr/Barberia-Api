package com.alexandre.Barbearia_Api.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDTO(
        @NotBlank
        @Email
        String email
) {}
