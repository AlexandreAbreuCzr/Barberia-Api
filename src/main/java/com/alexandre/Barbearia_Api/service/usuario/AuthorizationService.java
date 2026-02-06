package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.dto.usuario.AuthenticationDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioRegisterDTO;
import com.alexandre.Barbearia_Api.dto.usuario.LoginResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioJaExisteException;
import com.alexandre.Barbearia_Api.infra.security.TokenService;
import com.alexandre.Barbearia_Api.model.UserRole;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthorizationService(
            UsuarioRepository usuarioRepository,
            ObjectProvider<AuthenticationManager> authenticationManagerProvider,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManagerProvider = authenticationManagerProvider;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(AuthenticationDTO data) {

        String login = normalizeLogin(data.login());

        var authToken = new UsernamePasswordAuthenticationToken(login, data.password());
        var auth = authenticationManagerProvider.getObject().authenticate(authToken);

        var usuario = (Usuario) auth.getPrincipal();
        String token = tokenService.generateToken(usuario);

        return new LoginResponseDTO(token);
    }

    public ResponseEntity<?> register(UsuarioRegisterDTO data) {

        String username = normalizeUsername(data.username());
        String email = normalizeEmail(data.email());
        String telefone = normalizeTelefone(data.telefone());

        if (usuarioRepository.existsByUsername(username)) {
            throw new UsuarioJaExisteException("Username já está em uso");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new UsuarioJaExisteException("Email já está em uso");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());

        Usuario usuario = new Usuario(
                username,
                data.name().trim(),
                email,
                encryptedPassword,
                UserRole.USER
        );
        if (telefone != null && !telefone.isBlank()) {
            usuario.setTelefone(telefone);
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok().build();
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        String normalized = normalizeLogin(login);

        Usuario usuario = usuarioRepository.findByUsername(normalized)
                .or(() -> usuarioRepository.findByEmail(normalizeEmail(normalized)))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));

        // anti-enumeração: mesma mensagem
        if (!Boolean.TRUE.equals(usuario.isStatus())) {
            throw new UsernameNotFoundException("Credenciais inválidas");
        }

        return usuario;
    }

    private String normalizeLogin(String login) {
        if (login == null) return "";
        return login.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        if (username == null) return "";
        return username.trim().toLowerCase();
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }

    private String normalizeTelefone(String telefone) {
        if (telefone == null) return null;
        String cleaned = telefone.replaceAll("\\D", "");
        return cleaned.isBlank() ? null : cleaned;
    }
}
