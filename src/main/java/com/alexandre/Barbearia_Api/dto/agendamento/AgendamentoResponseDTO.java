package com.alexandre.Barbearia_Api.dto.agendamento;

import com.alexandre.Barbearia_Api.model.AgendamentoStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AgendamentoResponseDTO (
        Long id,
        String clienteUsername,
        String barbeiroUsername,
        Long servicoId,
        LocalDate data,
        LocalTime hora,
        AgendamentoStatus agendamentoStatus,
        LocalDateTime dataDeCriacao,
        LocalDateTime dataDeModificacao
){}