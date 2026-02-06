package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.PasswordResetCode;
import com.alexandre.Barbearia_Api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findFirstByUsuarioAndUsedFalseOrderByCreatedAtDesc(Usuario usuario);
    List<PasswordResetCode> findByUsuarioAndUsedFalse(Usuario usuario);
}
