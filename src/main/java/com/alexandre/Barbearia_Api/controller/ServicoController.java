package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.servico.ServicoCreateDTO;
import com.alexandre.Barbearia_Api.dto.servico.ServicoResponseDTO;
import com.alexandre.Barbearia_Api.dto.servico.update.*;
import com.alexandre.Barbearia_Api.service.servico.ServicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/servico")
public class ServicoController {
    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status
    ) {
        return ResponseEntity.ok(servicoService.find(name, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> findById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(servicoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ServicoResponseDTO> create(
            @Valid @RequestBody ServicoCreateDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicoService.create(dto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServicoResponseDTO> createWithImage(
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam Integer duracaoEmMinutos,
            @RequestParam(required = false, defaultValue = "50.0") BigDecimal percentualComissao,
            @RequestPart(required = false) MultipartFile image
    ) throws java.io.IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicoService.createWithImage(name, price, duracaoEmMinutos, percentualComissao, image));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ServicoUpdateDTO dto
    ) {
        servicoService.update(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServicoResponseDTO> updateImage(
            @PathVariable @Positive Long id,
            @RequestPart MultipartFile image
    ) throws java.io.IOException {
        return ResponseEntity.ok(servicoService.updateImage(id, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive Long id
    ) {
        servicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
