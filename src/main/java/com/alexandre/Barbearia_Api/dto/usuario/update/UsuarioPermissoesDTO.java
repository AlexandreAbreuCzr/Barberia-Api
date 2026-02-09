package com.alexandre.Barbearia_Api.dto.usuario.update;

import com.alexandre.Barbearia_Api.model.AcessoPermissao;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UsuarioPermissoesDTO(
        @NotNull
        List<AcessoPermissao> permissoes
) {}
