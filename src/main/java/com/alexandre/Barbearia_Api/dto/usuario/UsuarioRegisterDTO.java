package com.alexandre.Barbearia_Api.dto.usuario;

import jakarta.validation.constraints.*;


public record UsuarioRegisterDTO(

        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9._]+$",
                message = "Username deve conter apenas letras, números, ponto ou underscore"
        )
        String username,

        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Email
        String email,

        @Pattern(
                regexp = "^[0-9]{10,15}$",
                message = "Telefone deve conter apenas números (10 a 15 dígitos)"
        )
        String telefone,

        @NotBlank
        @Size(min = 8)
        String password
) {}

