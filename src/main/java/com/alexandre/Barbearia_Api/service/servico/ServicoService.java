package com.alexandre.Barbearia_Api.service.servico;

import com.alexandre.Barbearia_Api.dto.servico.ServicoCreateDTO;
import com.alexandre.Barbearia_Api.dto.servico.ServicoResponseDTO;
import com.alexandre.Barbearia_Api.dto.servico.mapper.ServicoMapper;
import com.alexandre.Barbearia_Api.dto.servico.update.*;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoNotFoundException;
import com.alexandre.Barbearia_Api.infra.storage.FileStorageService;
import com.alexandre.Barbearia_Api.model.Servico;
import com.alexandre.Barbearia_Api.repository.ServicoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ServicoService {
    private final ServicoRepository servicoRepository;
    private final FileStorageService fileStorageService;

    public ServicoService(ServicoRepository servicoRepository, FileStorageService fileStorageService) {
        this.servicoRepository = servicoRepository;
        this.fileStorageService = fileStorageService;
    }

    public ServicoResponseDTO create(ServicoCreateDTO dto){
        Servico servico = new Servico();
        servico.setName(dto.name());
        servico.setPrice(dto.price());
        servico.setDuracaoMediaEmMinutos(dto.duracaoEmMinutos());
        Servico salvo = servicoRepository.save(servico);
        return ServicoMapper.toResponse(salvo);
    }

    public ServicoResponseDTO createWithImage(
            String name,
            java.math.BigDecimal price,
            Integer duracaoEmMinutos,
            MultipartFile image
    ) throws java.io.IOException {
        Servico servico = new Servico();
        servico.setName(name);
        servico.setPrice(price);
        servico.setDuracaoMediaEmMinutos(duracaoEmMinutos);
        String imageUrl = fileStorageService.storeServicoImage(image);
        if (imageUrl != null) {
            servico.setImageUrl(imageUrl);
        }
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
        Servico servico = getById(id);
        try {
            servicoRepository.delete(servico);
        } catch (DataIntegrityViolationException ex) {
            servico.setStatus(false);
            servicoRepository.save(servico);
        }
    }

    public void  update(Long id, ServicoUpdateDTO dto){

        Servico servico = getById(id);

        if (dto.name() != null) servico.setName(dto.name());
        if (dto.price() != null) servico.setPrice(dto.price());
        if (dto.duracaoEmMinutos() != null) servico.setDuracaoMediaEmMinutos(dto.duracaoEmMinutos());
        if (dto.status() != null) servico.setStatus(dto.status());
        servicoRepository.save(servico);
    }

    public ServicoResponseDTO updateImage(Long id, MultipartFile image) throws java.io.IOException {
        Servico servico = getById(id);
        String imageUrl = fileStorageService.storeServicoImage(image);
        if (imageUrl != null) {
            servico.setImageUrl(imageUrl);
            servicoRepository.save(servico);
        }
        return ServicoMapper.toResponse(servico);
    }

    private Servico getById(Long id){
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(ServicoNotFoundException::new);
        return servico;
    }

}
