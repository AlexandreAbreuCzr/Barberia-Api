package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.dto.usuario.create.FuncionarioCreateDTO;
import com.alexandre.Barbearia_Api.dto.usuario.mapper.UsuarioMapper;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioNameDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioRoleDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioStatusDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioTelefoneDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioMeUpdateDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioJaExisteException;
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
        Usuario autenticado = getUsuarioAutenticadoEntity();

        if (dto.role() == UserRole.ADMIN) {
            throw new AccessDeniedException("Nao e permitido atribuir o cargo ADMIN.");
        }
        if (dto.role() == UserRole.DONO && autenticado.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente ADMIN pode atribuir o cargo DONO.");
        }

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

    public void createFuncionario(FuncionarioCreateDTO dto) {
        requirePermission(AcessoPermissao.USUARIOS_GERIR);

        Usuario autenticado = getUsuarioAutenticadoEntity();
        UserRole role = dto.role() == null ? UserRole.FUNCIONARIO : dto.role();

        if (role == UserRole.ADMIN || role == UserRole.USER) {
            throw new AccessDeniedException("Cargo invalido para cadastro de funcionario.");
        }
        if (role == UserRole.DONO && autenticado.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente ADMIN pode criar um novo DONO.");
        }

        String username = normalizeUsername(dto.username());
        String email = normalizeEmail(dto.email());
        String telefone = normalizeTelefone(dto.telefone());

        if (usuarioRepository.existsByUsername(username)) {
            throw new UsuarioJaExisteException("Username ja esta em uso");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new UsuarioJaExisteException("Email ja esta em uso");
        }

        Usuario usuario = new Usuario(
                username,
                dto.name().trim(),
                email,
                passwordEncoder.encode(dto.password()),
                role
        );
        if (telefone != null && !telefone.isBlank()) {
            usuario.setTelefone(telefone);
        }

        if (dto.permissoes() != null && !dto.permissoes().isEmpty()) {
            usuario.setPermissoesEfetivas(new LinkedHashSet<>(dto.permissoes()));
        } else {
            usuario.setPermissoesEfetivas(Set.of());
        }

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
        List<UserRole> roles = List.of(UserRole.FUNCIONARIO, UserRole.DONO);
        return UsuarioMapper.toResponses(
                usuarioRepository.findByStatusAndRoleIn(true, roles)
                        .stream()
                        .filter(usuario -> usuario.getUsername() == null
                                || !usuario.getUsername().equalsIgnoreCase("ale"))
                        .toList()
        );
    }

    public List<UsuarioResponseDTO> findFuncionarios(String name, Boolean status, UserRole role) {
        requireAnyPermission(AcessoPermissao.USUARIOS_VISUALIZAR, AcessoPermissao.USUARIOS_GERIR);

        UserRole roleFiltroTmp = role;
        if (roleFiltroTmp != null && roleFiltroTmp != UserRole.FUNCIONARIO && roleFiltroTmp != UserRole.DONO) {
            roleFiltroTmp = null;
        }
        final UserRole roleFiltro = roleFiltroTmp;

        return UsuarioMapper.toResponses(
                usuarioRepository.findAll()
                        .stream()
                        .filter(usuario -> usuario.getRole() == UserRole.FUNCIONARIO || usuario.getRole() == UserRole.DONO)
                        .filter(usuario -> roleFiltro == null || usuario.getRole() == roleFiltro)
                        .filter(usuario -> name == null || usuario.getName().toLowerCase().contains(name.toLowerCase()))
                        .filter(usuario -> status == null || usuario.isStatus() == status)
                        .toList()
        );
    }

    public UsuarioResponseDTO getUsuarioAutenticado(){
        Usuario usuario = getUsuarioAutenticadoEntity();
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

    private Usuario getUsuarioAutenticadoEntity() {
        return (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void requireAnyPermission(AcessoPermissao... permissions) {
        Usuario autenticado = getUsuarioAutenticadoEntity();

        Set<AcessoPermissao> granted = autenticado.getPermissoesEfetivas();
        for (AcessoPermissao permission : permissions) {
            if (granted.contains(permission)) return;
        }
        throw new AccessDeniedException("Voce nao possui permissao para gerenciar usuarios.");
    }

    private void requirePermission(AcessoPermissao permission) {
        requireAnyPermission(permission);
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
