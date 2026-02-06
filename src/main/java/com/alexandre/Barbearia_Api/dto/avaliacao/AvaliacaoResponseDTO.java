package com.alexandre.Barbearia_Api.dto.avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResponseDTO(
        Long id,
        String nome,
        Integer nota,
        String comentario,
        LocalDateTime dataDeCriacao
) {}
