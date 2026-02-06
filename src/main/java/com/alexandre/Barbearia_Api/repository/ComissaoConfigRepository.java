package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.ComissaoConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComissaoConfigRepository extends JpaRepository<ComissaoConfig, Long> {
    Optional<ComissaoConfig> findFirstByOrderByIdAsc();
}
