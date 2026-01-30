package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.dto.usuario.AuthenticationDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioRegisterDTO;
import com.alexandre.Barbearia_Api.dto.usuario.LoginResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioJaExisteException;
import com.alexandre.Barbearia_Api.infra.security.TokenService;
import com.alexandre.Barbearia_Api.model.UserRole;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthorizationService(
            UsuarioRepository usuarioRepository,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(AuthenticationDTO data) {

        var authToken =
                new UsernamePasswordAuthenticationToken(data.username(), data.password());

        var auth = authenticationManager.authenticate(authToken);

        var token = tokenService.generateToken((Usuario) auth.getPrincipal());

        return new LoginResponseDTO(token);
    }

    public ResponseEntity<?> register(UsuarioRegisterDTO data) {
        if (usuarioRepository.existsByUsername(data.username())) {
            throw new UsuarioJaExisteException("Username já está em uso");
        }

        if (usuarioRepository.existsByEmail(data.email())) {
            throw new UsuarioJaExisteException("Email já está em uso");
        }

        if (usuarioRepository.findByUsername(data.username()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        if (usuarioRepository.findByEmail(data.email()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = passwordEncoder.encode(data.password());

        Usuario usuario = new Usuario(
                data.username(),
                data.name(),
                data.email(),
                encryptedPassword,
                data.role()
        );

        usuarioRepository.save(usuario);

        return ResponseEntity.ok().build();
    }

}

