package com.alexandre.Barbearia_Api.dto.servico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoResponseDTO (
        Long id,
        String name,
        BigDecimal price,
        Integer duracaoMediaEmMinutos,
        Boolean status,
        LocalDateTime dataDeCriacao,
        LocalDateTime dataDeModificacao
){}
