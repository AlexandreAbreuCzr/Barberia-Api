package com.alexandre.Barbearia_Api.service.usuario;

import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.dto.usuario.mapper.UsuarioMapper;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioNameDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioRoleDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioStatusDTO;
import com.alexandre.Barbearia_Api.dto.usuario.update.UsuarioTelefoneDTO;
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

        if (usuario.getRole() == UserRole.ADMIN) {
            return;
        }

        usuario.setRole(dto.role());
        usuarioRepository.save(usuario);
    }


    public void updateName(String username, UsuarioNameDTO dto){
        Usuario usuario = getByUsername(username);

        if (usuario.getName().equals(dto.name())) {
            return;
        }
        usuario.setName(dto.name());
        usuarioRepository.save(usuario);
    }

    public void updateTelefone(String username, UsuarioTelefoneDTO dto){
        Usuario usuario = getByUsername(username);
        usuario.setTelefone(dto.telefone());
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

    public List<UsuarioResponseDTO> find(String name, Boolean status, UserRole role){
        if (name != null && status != null && role != null)
            return mapUsuarios(usuarioRepository.findByNameContainingIgnoreCaseAndStatusAndRole(name, status, role));

        if (status != null && role != null)
            return mapUsuarios(usuarioRepository.findByStatusAndRole(status, role));

        if (name != null){
            return mapUsuarios(usuarioRepository.findByNameContainingIgnoreCase(name));
        }

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
        return usuarioRepository.findByUsername(username)
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
