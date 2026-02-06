package com.alexandre.Barbearia_Api.dto.indisponibilidade.mapper;

import com.alexandre.Barbearia_Api.dto.indisponibilidade.IndisponibilidadeResponseDTO;
import com.alexandre.Barbearia_Api.model.Indisponibilidade;

import java.util.List;

public class IndisponibilidadeMapper {
    public static IndisponibilidadeResponseDTO toResponse(Indisponibilidade indisponibilidade){
        return new IndisponibilidadeResponseDTO(
                indisponibilidade.getId(),
                indisponibilidade.getBarbeiro().getUsername(),
                indisponibilidade.getTipo(),
                indisponibilidade.getInicio(),
                indisponibilidade.getFim(),
                indisponibilidade.getDataDeCriacao(),
                indisponibilidade.getDataDeModificacao()
        );
    }

    public static List<IndisponibilidadeResponseDTO> toResponses(List<Indisponibilidade> indisponibilidades){
        return indisponibilidades.stream().map(IndisponibilidadeMapper::toResponse).toList();
    }
}
