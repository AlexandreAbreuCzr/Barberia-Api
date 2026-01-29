package com.alexandre.Barbearia_Api.dto.agendamento.mapper;

import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoResponseDTO;
import com.alexandre.Barbearia_Api.model.Agendamento;

public class AgendamentoMapper {
    public static AgendamentoResponseDTO toResponse(Agendamento agendamento){
        return new AgendamentoResponseDTO(
                agendamento.getId(),
                agendamento.getCliente().getId(),
                agendamento.getBarbeiro().getId(),
                agendamento.getServico().getId(),
                agendamento.getData(),
                agendamento.getHora(),
                agendamento.getAgendamentoStatus(),
                agendamento.getDataDeCriacao(),
                agendamento.getDataDeModificacao()
        );
    }
}
