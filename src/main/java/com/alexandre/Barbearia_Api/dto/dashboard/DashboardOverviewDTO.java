package com.alexandre.Barbearia_Api.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardOverviewDTO(
        LocalDate inicio,
        LocalDate fim,
        long diasAnalisados,
        long totalAgendamentos,
        long totalRequisitados,
        long totalAgendados,
        long totalConcluidos,
        long totalCancelados,
        long totalExpirados,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoCaixa,
        BigDecimal totalComissoes,
        BigDecimal ticketMedio,
        List<DashboardDailyPointDTO> serieDiaria,
        List<DashboardBarberRankingDTO> rankingBarbeiros
) {}
