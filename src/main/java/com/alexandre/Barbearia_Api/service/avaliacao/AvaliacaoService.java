package com.alexandre.Barbearia_Api.service.avaliacao;

import com.alexandre.Barbearia_Api.dto.avaliacao.AvaliacaoCreateDTO;
import com.alexandre.Barbearia_Api.dto.avaliacao.AvaliacaoResponseDTO;
import com.alexandre.Barbearia_Api.dto.avaliacao.mapper.AvaliacaoMapper;
import com.alexandre.Barbearia_Api.model.Avaliacao;
import com.alexandre.Barbearia_Api.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoService {
    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public AvaliacaoResponseDTO create(AvaliacaoCreateDTO dto) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNome(dto.nome());
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());
        Avaliacao salvo = avaliacaoRepository.save(avaliacao);
        return AvaliacaoMapper.toResponse(salvo);
    }

    public List<AvaliacaoResponseDTO> findAll() {
        return AvaliacaoMapper.toResponses(avaliacaoRepository.findAllByOrderByDataDeCriacaoDesc());
    }
}
