package com.alexandre.Barbearia_Api.dto.agendamento.disponibilidade;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AgendamentoDisponibilidadeDiaDTO(
        LocalDate data,
        boolean disponivel,
        List<LocalTime> horariosDisponiveis
) {
}
