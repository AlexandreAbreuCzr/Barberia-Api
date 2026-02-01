package com.alexandre.Barbearia_Api.dto.agendamento.mapper;

import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoResponseDTO;
import com.alexandre.Barbearia_Api.model.Agendamento;

import java.util.List;

public class AgendamentoMapper {
    public static AgendamentoResponseDTO toResponse(Agendamento agendamento){
        return new AgendamentoResponseDTO(
                agendamento.getId(),
                agendamento.getCliente().getUsername(),
                agendamento.getBarbeiro().getUsername(),
                agendamento.getServico().getId(),
                agendamento.getData(),
                agendamento.getHora(),
                agendamento.getAgendamentoStatus(),
                agendamento.getDataDeCriacao(),
                agendamento.getDataDeModificacao()
        );
    }
    public static List<AgendamentoResponseDTO> toResponses(List<Agendamento> agendamentos){
        return agendamentos.stream().map(AgendamentoMapper::toResponse).toList();
    }
}
