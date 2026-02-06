package com.alexandre.Barbearia_Api.dto.caixa;

import com.alexandre.Barbearia_Api.model.CaixaTipo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CaixaResponseDTO(
        Long id,
        CaixaTipo tipo,
        String descricao,
        BigDecimal valor,
        BigDecimal valorBarbeiro,
        BigDecimal valorBarbearia,
        BigDecimal percentualComissao,
        Long agendamentoId,
        String barbeiroUsername,
        String servicoNome,
        LocalDateTime dataDeCriacao
) {}
