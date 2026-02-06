package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.comissao.ComissaoResponseDTO;
import com.alexandre.Barbearia_Api.dto.comissao.ComissaoUpdateDTO;
import com.alexandre.Barbearia_Api.dto.comissao.ComissaoConfigDTO;
import com.alexandre.Barbearia_Api.dto.comissao.ComissaoTaxaUpdateDTO;
import com.alexandre.Barbearia_Api.service.comissao.ComissaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/comissao")
public class ComissaoController {

    private final ComissaoService comissaoService;

    public ComissaoController(ComissaoService comissaoService) {
        this.comissaoService = comissaoService;
    }

    @GetMapping
    public ResponseEntity<List<ComissaoResponseDTO>> find(
            @RequestParam(required = false) String barbeiroUsername,
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim
    ) {
        return ResponseEntity.ok(comissaoService.find(barbeiroUsername, inicio, fim));
    }

    @GetMapping("/taxa")
    public ResponseEntity<ComissaoConfigDTO> getTaxaGlobal() {
        return ResponseEntity.ok(comissaoService.getTaxaGlobal());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ComissaoResponseDTO> updatePercentual(
            @PathVariable Long id,
            @Valid @RequestBody ComissaoUpdateDTO dto
    ) {
        return ResponseEntity.ok(comissaoService.updatePercentual(id, dto));
    }

    @PatchMapping("/taxa")
    public ResponseEntity<ComissaoConfigDTO> updateTaxaGlobal(
            @Valid @RequestBody ComissaoTaxaUpdateDTO dto
    ) {
        return ResponseEntity.ok(comissaoService.updateTaxaGlobal(dto));
    }
}
