package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.usuario.AuthenticationDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioRegisterDTO;
import com.alexandre.Barbearia_Api.service.usuario.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final AuthorizationService authorizationService;

    public AuthenticationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO data){
        return ResponseEntity.ok(authorizationService.login(data));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioRegisterDTO data){
        return ResponseEntity.ok(authorizationService.register(data));
    }


}
