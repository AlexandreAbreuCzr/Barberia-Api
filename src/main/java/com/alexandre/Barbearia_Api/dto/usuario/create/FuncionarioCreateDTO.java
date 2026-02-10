package com.alexandre.Barbearia_Api.dto.usuario.create;

import com.alexandre.Barbearia_Api.model.AcessoPermissao;
import com.alexandre.Barbearia_Api.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FuncionarioCreateDTO(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9._]+$",
                message = "Username deve conter apenas letras, numeros, ponto ou underscore"
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
                message = "Telefone deve conter apenas numeros (10 a 15 digitos)"
        )
        String telefone,

        @NotBlank
        @Size(min = 8)
        String password,

        UserRole role,
        List<AcessoPermissao> permissoes
) {}
