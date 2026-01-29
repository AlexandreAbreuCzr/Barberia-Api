package com.alexandre.Barbearia_Api.dto.agendamento.update;

import com.alexandre.Barbearia_Api.model.AgendamentoStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoUpdateDTO(
        LocalDate data,
        LocalTime hora,
        AgendamentoStatus agendamentoStatus
){}
