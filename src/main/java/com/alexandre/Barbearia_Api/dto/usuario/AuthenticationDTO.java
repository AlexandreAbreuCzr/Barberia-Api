package com.alexandre.Barbearia_Api.dto.usuario;


import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(

        @NotBlank(message = "Usuário ou email é obrigatório")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        String password
) {}
