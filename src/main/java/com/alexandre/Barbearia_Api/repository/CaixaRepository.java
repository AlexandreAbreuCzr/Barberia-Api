package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.Caixa;
import com.alexandre.Barbearia_Api.model.CaixaTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {
    Optional<Caixa> findByAgendamento_Id(Long id);
    List<Caixa> findByDataDeCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Caixa> findByTipo(CaixaTipo tipo);
    List<Caixa> findByTipoAndDataDeCriacaoBetween(CaixaTipo tipo, LocalDateTime inicio, LocalDateTime fim);
}
