package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.usuario.AuthenticationDTO;
import com.alexandre.Barbearia_Api.dto.usuario.PasswordResetConfirmDTO;
import com.alexandre.Barbearia_Api.dto.usuario.PasswordResetRequestDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioRegisterDTO;
import com.alexandre.Barbearia_Api.infra.security.RateLimitService;
import com.alexandre.Barbearia_Api.service.usuario.AuthorizationService;
import com.alexandre.Barbearia_Api.service.usuario.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final AuthorizationService authorizationService;
    private final PasswordResetService passwordResetService;
    private final RateLimitService rateLimitService;

    public AuthenticationController(
            AuthorizationService authorizationService,
            PasswordResetService passwordResetService,
            RateLimitService rateLimitService
    ) {
        this.authorizationService = authorizationService;
        this.passwordResetService = passwordResetService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO data, HttpServletRequest request){
        rateLimitService.check("login:" + getClientIp(request), 8, Duration.ofMinutes(1));
        return ResponseEntity.ok(authorizationService.login(data));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioRegisterDTO data, HttpServletRequest request){
        rateLimitService.check("register:" + getClientIp(request), 6, Duration.ofMinutes(10));
        return ResponseEntity.ok(authorizationService.register(data));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgot(@Valid @RequestBody PasswordResetRequestDTO dto, HttpServletRequest request){
        rateLimitService.check("pwd-forgot:" + getClientIp(request), 5, Duration.ofMinutes(15));
        passwordResetService.sendCode(dto.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> reset(@Valid @RequestBody PasswordResetConfirmDTO dto, HttpServletRequest request){
        rateLimitService.check("pwd-reset:" + getClientIp(request), 6, Duration.ofMinutes(15));
        passwordResetService.resetPassword(dto.email(), dto.code(), dto.newPassword());
        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
