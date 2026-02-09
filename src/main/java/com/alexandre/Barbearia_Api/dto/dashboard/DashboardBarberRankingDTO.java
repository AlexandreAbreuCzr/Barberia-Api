package com.alexandre.Barbearia_Api.dto.dashboard;

import java.math.BigDecimal;

public record DashboardBarberRankingDTO(
        String barbeiroUsername,
        String barbeiroNome,
        long agendamentos,
        long concluidos,
        BigDecimal faturamento,
        BigDecimal comissao
) {}
