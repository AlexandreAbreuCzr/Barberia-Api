package com.alexandre.Barbearia_Api.dto.usuario.mapper;

import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.model.Usuario;

public class UsuarioMapper {
    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getRole().name(),
                usuario.getDataDeCriacao(),
                usuario.getDataDeModificacao(),
                usuario.isStatus()
        );
    }
}
