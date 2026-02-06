package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.infra.exceptions.usuario.PasswordResetCodeInvalidException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.PasswordResetCode;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.PasswordResetCodeRepository;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import com.alexandre.Barbearia_Api.service.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetCodeRepository resetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password.reset.expiration-minutes:15}")
    private long expirationMinutes;

    public PasswordResetService(
            UsuarioRepository usuarioRepository,
            PasswordResetCodeRepository resetRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.resetRepository = resetRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void sendCode(String email) {
        String normalized = normalizeEmail(email);
        Usuario usuario = usuarioRepository.findByEmail(normalized)
                .orElse(null);
        if (usuario == null) {
            // Anti-enumeração: não revela se o email existe
            return;
        }

        invalidateCodes(usuario);

        String code = generateCode();
        PasswordResetCode reset = new PasswordResetCode();
        reset.setUsuario(usuario);
        reset.setCode(passwordEncoder.encode(code));
        reset.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        reset.setUsed(false);
        resetRepository.save(reset);

        String subject = "Código de recuperação de senha";
        String body = "Seu código para redefinir a senha: " + code + "\n\nSe você não solicitou, ignore.";
        emailService.send(usuario.getEmail(), subject, body);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        String normalized = normalizeEmail(email);
        Usuario usuario = usuarioRepository.findByEmail(normalized)
                .orElseThrow(PasswordResetCodeInvalidException::new);

        PasswordResetCode reset = resetRepository
                .findFirstByUsuarioAndUsedFalseOrderByCreatedAtDesc(usuario)
                .orElseThrow(PasswordResetCodeInvalidException::new);

        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PasswordResetCodeInvalidException();
        }
        if (!passwordEncoder.matches(code.trim(), reset.getCode())) {
            throw new PasswordResetCodeInvalidException();
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        reset.setUsed(true);
        resetRepository.save(reset);
        invalidateCodes(usuario);
    }

    private void invalidateCodes(Usuario usuario) {
        List<PasswordResetCode> codes = resetRepository.findByUsuarioAndUsedFalse(usuario);
        if (codes.isEmpty()) return;
        codes.forEach(code -> code.setUsed(true));
        resetRepository.saveAll(codes);
    }

    private String generateCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(value);
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }
}
