package com.alexandre.Barbearia_Api.service.indisponibilidade;

import com.alexandre.Barbearia_Api.dto.indisponibilidade.IndisponibilidadeCreateDTO;
import com.alexandre.Barbearia_Api.dto.indisponibilidade.IndisponibilidadeResponseDTO;
import com.alexandre.Barbearia_Api.dto.indisponibilidade.mapper.IndisponibilidadeMapper;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.indisponibilidade.IndisponibilidadeNotFoundException;
import com.alexandre.Barbearia_Api.infra.exceptions.indisponibilidade.IndisponibilidadeNotFoundInicioFimException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNaoBarbeiroException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.Indisponibilidade;
import com.alexandre.Barbearia_Api.model.TipoIndisponibilidade;
import com.alexandre.Barbearia_Api.model.UserRole;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.IndisponibilidadeRepository;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import com.alexandre.Barbearia_Api.service.usuario.UsuarioService;
import com.alexandre.Barbearia_Api.specificifications.IndisponibilidadeSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IndisponibilidadeService {

    private final IndisponibilidadeRepository indisponibilidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public IndisponibilidadeService(
            IndisponibilidadeRepository indisponibilidadeRepository,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService
    ) {
        this.indisponibilidadeRepository = indisponibilidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    //=======================
    //         CRUD
    //=======================


    //Create
    public IndisponibilidadeResponseDTO create(IndisponibilidadeCreateDTO dto){
        Indisponibilidade indisponibilidade = new Indisponibilidade();

        validarIntervalo(dto.inicio(), dto.fim());

        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        UserRole role = UserRole.from(usuario.role());
        String alvoUsername = dto.barbeiroUsername();

        if (isGestaoAgendaRole(role)) {
            if (alvoUsername == null || alvoUsername.isBlank()) {
                throw new UsuarioNaoBarbeiroException("Informe o barbeiro responsável.");
            }
        } else if (role == UserRole.BARBEIRO) {
            alvoUsername = usuario.username();
        } else {
            throw new UsuarioNaoBarbeiroException();
        }

        indisponibilidade.setBarbeiro(getUsuarioByUsername(alvoUsername));
        indisponibilidade.setTipo(dto.tipo());
        indisponibilidade.setInicio(dto.inicio());
        indisponibilidade.setFim(dto.fim());

        Indisponibilidade salvo =indisponibilidadeRepository.save(indisponibilidade);
        return IndisponibilidadeMapper.toResponse(salvo);
    }

    //Find & Finds

    public IndisponibilidadeResponseDTO findById(Long id){
        return IndisponibilidadeMapper.toResponse(getById(id));
    }

    public List<IndisponibilidadeResponseDTO> find(
            String barbeiroUsername,
            LocalDateTime inicio,
            LocalDateTime fim,
            TipoIndisponibilidade tipo
    ) {
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        if (!isGestaoAgendaRole(UserRole.from(usuario.role()))) {
            barbeiroUsername = usuario.username();
        }
        Specification<Indisponibilidade> spec = Specification.unrestricted();


        if (barbeiroUsername != null && !barbeiroUsername.isBlank()) {
            spec = spec.and(IndisponibilidadeSpecification.barbeiroUsername(barbeiroUsername));
        }

        if (tipo != null) {
            spec = spec.and(IndisponibilidadeSpecification.tipo(tipo));
        }

        if (inicio != null && fim != null) {
            spec = spec.and(IndisponibilidadeSpecification.overlap(inicio, fim));
        } else if (inicio != null || fim != null) {
            throw new IndisponibilidadeNotFoundInicioFimException();
        }

        return IndisponibilidadeMapper.toResponses(indisponibilidadeRepository.findAll(spec));
    }
    // Delete
    public void delete(Long id){
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Indisponibilidade indisponibilidade = getById(id);

        if (!isGestaoAgendaRole(UserRole.from(usuario.role()))) {
            String barbeiroUsername = indisponibilidade.getBarbeiro() != null
                    ? indisponibilidade.getBarbeiro().getUsername()
                    : null;
            if (barbeiroUsername == null || !barbeiroUsername.equalsIgnoreCase(usuario.username())) {
                throw new UsuarioNaoBarbeiroException("Você não pode remover indisponibilidades de outro barbeiro.");
            }
        }

        indisponibilidadeRepository.delete(indisponibilidade);
    }

    //=======================
    //    METODOS PRIVADOS
    //=======================

    private Usuario getUsuarioByUsername(String username){
        return usuarioRepository.findByUsername(username)
                .orElseThrow(UsuarioNotFoundException::new);
    }

    private Indisponibilidade getById(Long id){
        Indisponibilidade indisponibilidade = indisponibilidadeRepository.findById(id)
                .orElseThrow(IndisponibilidadeNotFoundException::new);
        return indisponibilidade;
    }


    private void validarIntervalo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) throw new IndisponibilidadeNotFoundInicioFimException();
        if (!fim.isAfter(inicio)) {
            throw new IndisponibilidadeNotFoundInicioFimException("Fim deve ser depois do início.");
        }
    }

    private boolean isGestaoAgendaRole(UserRole role) {
        return role == UserRole.ADMIN || role == UserRole.GERENTE;
    }
}
