package com.alexandre.Barbearia_Api.dto.agendamento.disponibilidade;

import java.time.LocalDate;
import java.util.List;

public record AgendamentoDisponibilidadeResponseDTO(
        String barbeiroUsername,
        Long servicoId,
        Integer duracaoEmMinutos,
        LocalDate inicio,
        LocalDate fim,
        List<AgendamentoDisponibilidadeDiaDTO> dias
) {
}
