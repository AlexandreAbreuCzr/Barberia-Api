package com.alexandre.Barbearia_Api.dto.usuario.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioMeUpdateDTO(
        @Size(min = 3, max = 50)
        String name,

        @Pattern(
                regexp = "^[0-9]{10,15}$",
                message = "Telefone deve conter apenas números (10 a 15 dígitos)"
        )
        String telefone,

        @Size(min = 8)
        String password
) {}
