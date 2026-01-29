package com.alexandre.Barbearia_Api.dto.servico.mapper;

import com.alexandre.Barbearia_Api.dto.servico.ServicoResponseDTO;
import com.alexandre.Barbearia_Api.model.Servico;

public class ServicoMapper {
    public static ServicoResponseDTO toResponse(Servico servico){
        return new ServicoResponseDTO(
                servico.getId(),
                servico.getName(),
                servico.getPrice(),
                servico.getDuracaoMediaEmMinutos(),
                servico.isStatus(),
                servico.getDataDeCriacao(),
                servico.getDataDeModificacao()
        );
    }
}
