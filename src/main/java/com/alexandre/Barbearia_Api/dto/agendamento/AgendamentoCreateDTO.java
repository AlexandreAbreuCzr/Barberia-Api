package com.alexandre.Barbearia_Api.dto.agendamento;

import com.alexandre.Barbearia_Api.model.Usuario;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AgendamentoCreateDTO (
        @NotNull String clienteName,
        @NotNull String barbeiroName,
        @NotNull Long servicoId,
        @NotNull LocalDate data,
        @NotNull LocalTime hora
){}
