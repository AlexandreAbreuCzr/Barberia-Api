package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.UserRole;
import com.alexandre.Barbearia_Api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByName(String name);

    List<Usuario> findByStatus(boolean status);
    List<Usuario> findByRole(UserRole role);
    List<Usuario> findByStatusAndRole(boolean status, UserRole role);
}

