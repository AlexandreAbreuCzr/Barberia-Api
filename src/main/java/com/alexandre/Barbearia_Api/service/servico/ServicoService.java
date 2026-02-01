package com.alexandre.Barbearia_Api.service.servico;

import com.alexandre.Barbearia_Api.dto.servico.ServicoCreateDTO;
import com.alexandre.Barbearia_Api.dto.servico.ServicoResponseDTO;
import com.alexandre.Barbearia_Api.dto.servico.mapper.ServicoMapper;
import com.alexandre.Barbearia_Api.dto.servico.update.*;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoNotFoundException;
import com.alexandre.Barbearia_Api.model.Servico;
import com.alexandre.Barbearia_Api.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {
    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public ServicoResponseDTO create(ServicoCreateDTO dto){
        Servico servico = new Servico();
        servico.setName(dto.name());
        servico.setPrice(dto.price());
        servico.setDuracaoMediaEmMinutos(dto.duracaoEmMinutos());
        Servico salvo = servicoRepository.save(servico);
        return ServicoMapper.toResponse(salvo);
    }

    public ServicoResponseDTO findById(Long id){
        return ServicoMapper.toResponse(getById(id));
    }

    public List<ServicoResponseDTO> find(String name, Boolean status){

        if (name != null && status != null)
            return ServicoMapper.toResponses(servicoRepository.findByNameContainingIgnoreCaseAndStatus(name.trim(), status));
        if (name != null)
            return ServicoMapper.toResponses(servicoRepository.findByNameContainingIgnoreCase(name.trim()));
        if (status != null)
            return ServicoMapper.toResponses(servicoRepository.findByStatus(status));

        return ServicoMapper.toResponses(servicoRepository.findAll());
    }

    public void delete(Long id){
        servicoRepository.delete(getById(id));
    }

    public void  update(Long id, ServicoUpdateDTO dto){

        Servico servico = getById(id);

        if (dto.name() != null) servico.setName(dto.name());
        if (dto.price() != null) servico.setPrice(dto.price());
        if (dto.duracaoEmMinutos() != null) servico.setDuracaoMediaEmMinutos(dto.duracaoEmMinutos());
        if (dto.status() != null) servico.setStatus(dto.status());
        servicoRepository.save(servico);
    }

    // Metodos privados

    private Servico getById(Long id){
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(ServicoNotFoundException::new);
        return servico;
    }

}
