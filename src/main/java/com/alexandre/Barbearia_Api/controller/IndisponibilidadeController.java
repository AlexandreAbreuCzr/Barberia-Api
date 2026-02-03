package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.indisponibilidade.IndisponibilidadeCreateDTO;
import com.alexandre.Barbearia_Api.dto.indisponibilidade.IndisponibilidadeResponseDTO;
import com.alexandre.Barbearia_Api.model.TipoIndisponibilidade;
import com.alexandre.Barbearia_Api.service.indisponibilidade.IndisponibilidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/indisponibilidade")
public class IndisponibilidadeController {
    private final IndisponibilidadeService indisponibilidadeService;

    public IndisponibilidadeController(IndisponibilidadeService indisponibilidadeService) {
        this.indisponibilidadeService = indisponibilidadeService;
    }

    @PostMapping
    public ResponseEntity<IndisponibilidadeResponseDTO> create(@Valid @RequestBody IndisponibilidadeCreateDTO dto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(indisponibilidadeService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<IndisponibilidadeResponseDTO>> find(
        @RequestParam(required = false) String barbeiroUsername,
        @RequestParam(required = false) LocalDateTime inicio,
        @RequestParam(required = false) LocalDateTime fim,
        @RequestParam(required = false) TipoIndisponibilidade tipo
    ){
        return ResponseEntity.ok(indisponibilidadeService.find(barbeiroUsername, inicio, fim, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndisponibilidadeResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(indisponibilidadeService.findById(id));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        indisponibilidadeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
