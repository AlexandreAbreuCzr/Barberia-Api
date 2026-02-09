package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.caixa.CaixaCreateDTO;
import com.alexandre.Barbearia_Api.dto.caixa.CaixaResponseDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoCreateDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoPreviewDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoResponseDTO;
import com.alexandre.Barbearia_Api.model.CaixaFechamentoPeriodo;
import com.alexandre.Barbearia_Api.model.CaixaTipo;
import com.alexandre.Barbearia_Api.service.caixa.CaixaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/caixa")
public class CaixaController {

    private final CaixaService caixaService;

    public CaixaController(CaixaService caixaService) {
        this.caixaService = caixaService;
    }

    @GetMapping
    public ResponseEntity<List<CaixaResponseDTO>> find(
            @RequestParam(required = false) CaixaTipo tipo,
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim
    ) {
        return ResponseEntity.ok(caixaService.find(tipo, inicio, fim));
    }

    @PostMapping
    public ResponseEntity<CaixaResponseDTO> create(@Valid @RequestBody CaixaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caixaService.createManual(dto));
    }

    @GetMapping("/fechamento/preview")
    public ResponseEntity<CaixaFechamentoPreviewDTO> preview(
            @RequestParam CaixaFechamentoPeriodo periodo,
            @RequestParam(required = false) LocalDate referencia,
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim
    ) {
        return ResponseEntity.ok(caixaService.previewFechamento(periodo, referencia, inicio, fim));
    }

    @GetMapping("/fechamento")
    public ResponseEntity<List<CaixaFechamentoResponseDTO>> findFechamentos(
            @RequestParam(required = false) CaixaFechamentoPeriodo periodo,
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim
    ) {
        return ResponseEntity.ok(caixaService.findFechamentos(periodo, inicio, fim));
    }

    @PostMapping("/fechamento")
    public ResponseEntity<CaixaFechamentoResponseDTO> fechar(
            @Valid @RequestBody CaixaFechamentoCreateDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caixaService.fechar(dto));
    }
}
