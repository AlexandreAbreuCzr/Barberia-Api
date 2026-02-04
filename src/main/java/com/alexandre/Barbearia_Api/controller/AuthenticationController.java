package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.usuario.AuthenticationDTO;
import com.alexandre.Barbearia_Api.dto.usuario.PasswordResetConfirmDTO;
import com.alexandre.Barbearia_Api.dto.usuario.PasswordResetRequestDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioRegisterDTO;
import com.alexandre.Barbearia_Api.service.usuario.AuthorizationService;
import com.alexandre.Barbearia_Api.service.usuario.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final AuthorizationService authorizationService;
    private final PasswordResetService passwordResetService;

    public AuthenticationController(
            AuthorizationService authorizationService,
            PasswordResetService passwordResetService
    ) {
        this.authorizationService = authorizationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO data){
        return ResponseEntity.ok(authorizationService.login(data));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioRegisterDTO data){
        return ResponseEntity.ok(authorizationService.register(data));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgot(@Valid @RequestBody PasswordResetRequestDTO dto){
        passwordResetService.sendCode(dto.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> reset(@Valid @RequestBody PasswordResetConfirmDTO dto){
        passwordResetService.resetPassword(dto.email(), dto.code(), dto.newPassword());
        return ResponseEntity.ok().build();
    }


}
