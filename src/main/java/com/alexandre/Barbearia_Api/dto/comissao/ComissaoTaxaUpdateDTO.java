package com.alexandre.Barbearia_Api.dto.comissao;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ComissaoTaxaUpdateDTO(
        @NotNull BigDecimal percentual,
        Boolean aplicarEmTodas
) {}
