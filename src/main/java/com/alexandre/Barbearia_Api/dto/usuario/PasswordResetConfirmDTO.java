package com.alexandre.Barbearia_Api.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmDTO(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 4, max = 10)
        String code,
        @NotBlank
        @Size(min = 6, max = 100)
        String newPassword
) {}
