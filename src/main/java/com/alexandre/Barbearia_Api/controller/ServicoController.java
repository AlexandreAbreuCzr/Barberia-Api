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

import java.util.List;

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

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ServicoUpdateDTO dto
    ) {
        servicoService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive Long id
    ) {
        servicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
