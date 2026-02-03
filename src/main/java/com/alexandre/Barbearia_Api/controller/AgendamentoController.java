package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoCreateDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoResponseDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.mapper.AgendamentoMapper;
import com.alexandre.Barbearia_Api.dto.agendamento.update.AgendamentoUpdateDTO;
import com.alexandre.Barbearia_Api.model.AgendamentoStatus;
import com.alexandre.Barbearia_Api.service.agendamento.AgendamentoService;
import com.alexandre.Barbearia_Api.specificifications.AgendamentoSpecification;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agendamento")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    // Criar agendamento
    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> create(
            @Valid @RequestBody AgendamentoCreateDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(agendamentoService.create(dto));
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.findById(id));
    }

    // Agendamentos do usuário autenticado (cliente ou barbeiro)
    @GetMapping("/me")
    public ResponseEntity<List<AgendamentoResponseDTO>> getAutenticado() {
        return ResponseEntity.ok(agendamentoService.getAutenticado());
    }

    // Atualizar agendamento
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoUpdateDTO dto
    ) {
        agendamentoService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    // Aceitar agendamento (barbeiro)
    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<Void> aceitar(@PathVariable Long id) {
        agendamentoService.aceitar(id);
        return ResponseEntity.ok().build();
    }

    // Cancelar agendamento
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        agendamentoService.cancelar(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Void> concluir(@PathVariable Long id){
        agendamentoService.finalizar(id);
        return ResponseEntity.ok().build();
    }

    // Deletar (somente se ainda estiver REQUISITADO)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agendamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //finds
    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> find(
            @RequestParam(required = false) String clienteUserName,
            @RequestParam(required = false) String barbeiroUserName,
            @RequestParam(required = false) Long servicoId,
            @RequestParam(required = false) LocalDate data,
            @RequestParam(required = false) LocalTime hora,
            @RequestParam(required = false) AgendamentoStatus status
    ) {
        return ResponseEntity.ok(
                agendamentoService.find(
                        clienteUserName,
                        barbeiroUserName,
                        servicoId,
                        data,
                        hora,
                        status
                )
        );
    }

}
