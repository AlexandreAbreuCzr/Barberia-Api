package com.alexandre.Barbearia_Api.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardDailyPointDTO(
        LocalDate data,
        long agendamentos,
        long concluidos,
        BigDecimal entradas,
        BigDecimal saidas,
        BigDecimal saldo
) {}
