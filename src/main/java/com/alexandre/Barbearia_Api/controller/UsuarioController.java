package com.alexandre.Barbearia_Api.controller;

import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioNameDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioRoleDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioStatusDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioTelefoneDTO;
import com.alexandre.Barbearia_Api.model.UserRole;
import com.alexandre.Barbearia_Api.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(){
        return ResponseEntity.ok(usuarioService.getUsuarioAutenticado());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<UsuarioResponseDTO>> find(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) UserRole userRole
    ) {
        return ResponseEntity.ok(usuarioService.find(name, status, userRole));
    }

    @GetMapping("/admin/{username}")
    public ResponseEntity<UsuarioResponseDTO> findByUsername(@PathVariable String username){
        return ResponseEntity.ok(usuarioService.findByUserName(username));
    }

    @PatchMapping("/admin/{username}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String username, @Valid @RequestBody UsuarioStatusDTO dto) {
        usuarioService.updateStatus(username, dto);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/admin/{username}/telefone")
    public ResponseEntity<Void> updateStatus(@PathVariable String username, @Valid @RequestBody UsuarioTelefoneDTO dto) {
        usuarioService.updateTelefone(username, dto);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/admin/{username}/name")
    public ResponseEntity<Void> updateName(@PathVariable String username, @Valid @RequestBody UsuarioNameDTO dto) {
        usuarioService.updateName(username, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/{username}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable String username,
            @Valid @RequestBody UsuarioRoleDTO dto
    ) {
        usuarioService.updateRole(username, dto);
        return ResponseEntity.ok().build();
    }

}
