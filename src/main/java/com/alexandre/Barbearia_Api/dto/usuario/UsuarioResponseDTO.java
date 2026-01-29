package com.alexandre.Barbearia_Api.dto.usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String name,
        String role,
        LocalDateTime dataDeCriacao,
        LocalDateTime dataDeModificacao,
        Boolean status
) { }

