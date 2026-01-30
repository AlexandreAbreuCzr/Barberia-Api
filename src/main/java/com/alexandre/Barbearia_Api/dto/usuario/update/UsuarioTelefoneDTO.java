package com.alexandre.Barbearia_Api.dto.usuario.update;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UsuarioTelefoneDTO(
        @Pattern(
                regexp = "^[0-9]{10,15}$",
                message = "Telefone deve conter apenas números (10 a 15 dígitos)"
        )
        String telefone
) {}
