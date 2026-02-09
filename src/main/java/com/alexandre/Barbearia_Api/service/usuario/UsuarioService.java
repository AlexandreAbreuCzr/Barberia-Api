package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.dto.usuario.mapper.UsuarioMapper;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioNameDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioRoleDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioStatusDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioTelefoneDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioMeUpdateDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioPermissoesDTO;
import com.alexandre.Barbearia_Api.model.AcessoPermissao;
import com.alexandre.Barbearia_Api.model.UserRole;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void updateRole(String username, UsuarioRoleDTO dto){
        requirePermission(AcessoPermissao.USUARIOS_ALTERAR_ROLE);

        Usuario usuario = getByUsername(username);

        if (usuario.getRole() == UserRole.ADMIN) {
            return;
        }

        usuario.setRole(dto.role());
        usuario.setPermissoesEfetivas(Set.of());
        usuarioRepository.save(usuario);
    }


    public void updateName(String username, UsuarioNameDTO dto){
        requirePermission(AcessoPermissao.USUARIOS_GERIR);

        Usuario usuario = getByUsername(username);

        if (usuario.getName().equals(dto.name())) {
            return;
        }
        usuario.setName(dto.name());
        usuarioRepository.save(usuario);
    }

    public void updateTelefone(String username, UsuarioTelefoneDTO dto){
        requirePermission(AcessoPermissao.USUARIOS_GERIR);

        Usuario usuario = getByUsername(username);
        usuario.setTelefone(dto.telefone());
        usuarioRepository.save(usuario);
    }


    public void updateStatus(String username, UsuarioStatusDTO dto){
        requirePermission(AcessoPermissao.USUARIOS_GERIR);

        Usuario usuario = getByUsername(username);
        usuario.setStatus(dto.status());
        usuarioRepository.save(usuario);
    }

    public void updatePermissoes(String username, UsuarioPermissoesDTO dto) {
        requirePermission(AcessoPermissao.USUARIOS_ALTERAR_PERMISSOES);

        Usuario usuario = getByUsername(username);

        if (usuario.getRole() == UserRole.ADMIN) {
            return;
        }

        Set<AcessoPermissao> requested = dto.permissoes() == null
                ? Set.of()
                : new LinkedHashSet<>(dto.permissoes());

        usuario.setPermissoesEfetivas(requested);
        usuarioRepository.save(usuario);
    }

    public UsuarioResponseDTO findByUserName(String username){
        requireAnyPermission(AcessoPermissao.USUARIOS_VISUALIZAR, AcessoPermissao.USUARIOS_GERIR);
        return UsuarioMapper.toResponse(getByUsername(username));
    }

    public List<UsuarioResponseDTO> find(String name, Boolean status, UserRole role){
        requireAnyPermission(AcessoPermissao.USUARIOS_VISUALIZAR, AcessoPermissao.USUARIOS_GERIR);

        if (name != null && status != null && role != null)
            return UsuarioMapper.toResponses(usuarioRepository.findByNameContainingIgnoreCaseAndStatusAndRole(name, status, role));

        if (status != null && role != null)
            return UsuarioMapper.toResponses(usuarioRepository.findByStatusAndRole(status, role));

        if (name != null){
            return UsuarioMapper.toResponses(usuarioRepository.findByNameContainingIgnoreCase(name));
        }

        if (status != null)
            return UsuarioMapper.toResponses(usuarioRepository.findByStatus(status));

        if (role != null)
            return UsuarioMapper.toResponses(usuarioRepository.findByRole(role));

        return UsuarioMapper.toResponses(usuarioRepository.findAll());
    }

    public List<UsuarioResponseDTO> findBarbeirosAtivos() {
        List<UserRole> roles = List.of(UserRole.BARBEIRO, UserRole.ADMIN);
        return UsuarioMapper.toResponses(
                usuarioRepository.findByStatusAndRoleIn(true, roles)
                        .stream()
                        .filter(usuario -> usuario.getUsername() == null
                                || !usuario.getUsername().equalsIgnoreCase("ale"))
                        .toList()
        );
    }

    public UsuarioResponseDTO getUsuarioAutenticado(){
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return UsuarioMapper.toResponse(usuario);
    }

    public void updateMe(UsuarioMeUpdateDTO dto) {
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Usuario atual = getByUsername(usuario.getUsername());

        if (dto.name() != null && !dto.name().isBlank()) {
            atual.setName(dto.name().trim());
        }
        if (dto.telefone() != null && !dto.telefone().isBlank()) {
            atual.setTelefone(dto.telefone());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            atual.setPassword(passwordEncoder.encode(dto.password()));
        }

        usuarioRepository.save(atual);
    }

    private Usuario getByUsername(String username){
        return usuarioRepository.findByUsername(username)
                .orElseThrow(UsuarioNotFoundException::new);
    }

    private void requireAnyPermission(AcessoPermissao... permissions) {
        Usuario autenticado = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Set<AcessoPermissao> granted = autenticado.getPermissoesEfetivas();
        for (AcessoPermissao permission : permissions) {
            if (granted.contains(permission)) return;
        }
        throw new AccessDeniedException("Voce nao possui permissao para gerenciar usuarios.");
    }

    private void requirePermission(AcessoPermissao permission) {
        requireAnyPermission(permission);
    }
}
