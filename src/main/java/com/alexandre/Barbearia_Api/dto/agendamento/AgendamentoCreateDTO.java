package com.alexandre.Barbearia_Api.dto.agendamento;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoCreateDTO (
        @NotNull String clienteUsername,
        String barbeiroUsername,
        @NotNull Long servicoId,
        @NotNull LocalDate data,
        @NotNull LocalTime hora
){}
