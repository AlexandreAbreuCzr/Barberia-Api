package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findAllByOrderByDataDeCriacaoDesc();
}
