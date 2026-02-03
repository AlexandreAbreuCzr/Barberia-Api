package com.alexandre.Barbearia_Api.dto.indisponibilidade;

import com.alexandre.Barbearia_Api.model.TipoIndisponibilidade;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public record IndisponibilidadeResponseDTO(
        Long id,
        String barbeiroUsername,
        TipoIndisponibilidade tipo,
        LocalDateTime inicio,
        LocalDateTime fim,
        LocalDateTime dataDeCriacao,
        LocalDateTime dataDeModificacao
){}