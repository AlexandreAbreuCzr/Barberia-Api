package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.CaixaFechamento;
import com.alexandre.Barbearia_Api.model.CaixaFechamentoPeriodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CaixaFechamentoRepository extends JpaRepository<CaixaFechamento, Long> {
    boolean existsByPeriodoAndDataInicioAndDataFim(
            CaixaFechamentoPeriodo periodo,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );

    List<CaixaFechamento> findAllByOrderByDataDeCriacaoDesc();
}
