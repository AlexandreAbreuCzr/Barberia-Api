package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.dto.usuario.mapper.UsuarioMapper;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioRoleDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioStatusDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.UserRole;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void updateRole(String username, UsuarioRoleDTO dto){
        Usuario usuario = getByUsername(username);
        usuario.setRole(dto.role());
        usuarioRepository.save(usuario);
    }

    public void updateStatus(String username, UsuarioStatusDTO dto){
        Usuario usuario = getByUsername(username);
        usuario.setStatus(dto.status());
        usuarioRepository.save(usuario);
    }

    public UsuarioResponseDTO findByUserName(String username){
        return UsuarioMapper.toResponse(getByUsername(username));
    }

    public List<UsuarioResponseDTO> find(Boolean status, UserRole role){
        if (status != null && role != null)
            return mapUsuarios(usuarioRepository.findByStatusAndRole(status, role));

        if (status != null)
            return mapUsuarios(usuarioRepository.findByStatus(status));

        if (role != null)
            return mapUsuarios(usuarioRepository.findByRole(role));

        return mapUsuarios(usuarioRepository.findAll());
    }

    private List<UsuarioResponseDTO> mapUsuarios(List<Usuario> usuarios){
        return usuarios.stream().map(UsuarioMapper::toResponse).toList();
    }

    private Usuario getByUsername(String username){
        return usuarioRepository.findByName(username.trim())
                .orElseThrow(UsuarioNotFoundException::new);
    }

    public UsuarioResponseDTO getUsuarioAutenticado(){
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return UsuarioMapper.toResponse(usuario);
    }
}
