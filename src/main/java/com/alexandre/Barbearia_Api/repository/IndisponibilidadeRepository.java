package com.alexandre.Barbearia_Api.repository;


import com.alexandre.Barbearia_Api.model.Indisponibilidade;
import com.alexandre.Barbearia_Api.model.TipoIndisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface IndisponibilidadeRepository
        extends JpaRepository<Indisponibilidade, Long>,
        JpaSpecificationExecutor<Indisponibilidade> {

    List<Indisponibilidade> findByBarbeiro_Username(String barbeiroUsername);
    List<Indisponibilidade> findByInicio(LocalDateTime inicio);
    List<Indisponibilidade> findByFim(LocalDateTime fim);
    List<Indisponibilidade> findByTipo(TipoIndisponibilidade tipo);
}
