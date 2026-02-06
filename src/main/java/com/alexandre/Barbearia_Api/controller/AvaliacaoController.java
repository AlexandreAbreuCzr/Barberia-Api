package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.avaliacao.AvaliacaoCreateDTO;
import com.alexandre.Barbearia_Api.dto.avaliacao.AvaliacaoResponseDTO;
import com.alexandre.Barbearia_Api.infra.security.RateLimitService;
import com.alexandre.Barbearia_Api.service.avaliacao.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {
    private final AvaliacaoService avaliacaoService;
    private final RateLimitService rateLimitService;

    public AvaliacaoController(AvaliacaoService avaliacaoService, RateLimitService rateLimitService) {
        this.avaliacaoService = avaliacaoService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping
    public ResponseEntity<List<AvaliacaoResponseDTO>> findAll() {
        return ResponseEntity.ok(avaliacaoService.findAll());
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> create(
            @Valid @RequestBody AvaliacaoCreateDTO dto,
            HttpServletRequest request
    ) {
        rateLimitService.check("review:" + getClientIp(request), 10, Duration.ofMinutes(10));
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.create(dto));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
