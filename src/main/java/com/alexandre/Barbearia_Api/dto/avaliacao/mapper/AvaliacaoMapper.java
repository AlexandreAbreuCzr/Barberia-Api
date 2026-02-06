package com.alexandre.Barbearia_Api.dto.avaliacao.mapper;

import com.alexandre.Barbearia_Api.dto.avaliacao.AvaliacaoResponseDTO;
import com.alexandre.Barbearia_Api.model.Avaliacao;

import java.util.List;

public class AvaliacaoMapper {
    public static AvaliacaoResponseDTO toResponse(Avaliacao avaliacao) {
        return new AvaliacaoResponseDTO(
                avaliacao.getId(),
                avaliacao.getNome(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataDeCriacao()
        );
    }

    public static List<AvaliacaoResponseDTO> toResponses(List<Avaliacao> avaliacoes) {
        return avaliacoes.stream().map(AvaliacaoMapper::toResponse).toList();
    }
}
