package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.Comissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComissaoRepository extends JpaRepository<Comissao, Long> {
    Optional<Comissao> findByAgendamento_Id(Long id);
    List<Comissao> findByBarbeiro_Username(String username);
    List<Comissao> findByBarbeiro_UsernameAndDataDeCriacaoBetween(String username, LocalDateTime inicio, LocalDateTime fim);
    List<Comissao> findByDataDeCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);
}
