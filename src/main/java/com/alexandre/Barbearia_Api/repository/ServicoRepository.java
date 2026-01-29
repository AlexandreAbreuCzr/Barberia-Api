package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByNameContainingIgnoreCase(String name);
    List<Servico> findByStatus(boolean status);
    List<Servico> findByNameContainingIgnoreCaseAndStatus(String name, boolean status);
}
