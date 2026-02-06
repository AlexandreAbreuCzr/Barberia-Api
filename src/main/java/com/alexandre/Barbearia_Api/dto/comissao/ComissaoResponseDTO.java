package com.alexandre.Barbearia_Api.dto.comissao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComissaoResponseDTO(
        Long id,
        Long agendamentoId,
        String barbeiroUsername,
        String barbeiroNome,
        String servicoNome,
        BigDecimal percentual,
        BigDecimal valor,
        LocalDateTime dataDeCriacao
) {}
