package com.alexandre.Barbearia_Api.dto.usuario;


import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
        @NotBlank
        String username,
        @NotBlank
        String password
) {}