package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = normalizeLogin(username);
        Usuario usuario = usuarioRepository.findByUsername(normalized)
                .or(() -> usuarioRepository.findByEmail(normalizeEmail(normalized)))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas"));

        if (!Boolean.TRUE.equals(usuario.isStatus())) {
            throw new UsernameNotFoundException("Credenciais invalidas");
        }

        return usuario;
    }

    private String normalizeLogin(String login) {
        if (login == null) return "";
        return login.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }
}
