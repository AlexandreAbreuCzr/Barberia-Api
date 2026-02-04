package com.alexandre.Barbearia_Api.service.agendamento;

import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoCreateDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoResponseDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.mapper.AgendamentoMapper;
import com.alexandre.Barbearia_Api.dto.agendamento.update.AgendamentoUpdateDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.*;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNaoBarbeiroException;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoNotFoundException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.*;
import com.alexandre.Barbearia_Api.repository.AgendamentoRepository;
import com.alexandre.Barbearia_Api.repository.ServicoRepository;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import com.alexandre.Barbearia_Api.service.usuario.UsuarioService;
import com.alexandre.Barbearia_Api.specificifications.AgendamentoSpecification;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgendamentoService {

    private final UsuarioService usuarioService;
    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoValidator validator;
    private final AgendamentoHorarioValidator horarioValidator;

    // Injeção

    public AgendamentoService(
            UsuarioService usuarioService,
            AgendamentoRepository agendamentoRepository,
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository,
            AgendamentoValidator validator,
            AgendamentoHorarioValidator horarioValidator
    ) {
        this.usuarioService = usuarioService;
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.validator = validator;
        this.horarioValidator = horarioValidator;
    }

    // Criador de agendamento

    public AgendamentoResponseDTO create(AgendamentoCreateDTO dto) {

        Usuario barbeiro = getUsuarioByUsername(dto.barbeiroUsername());
        Usuario cliente = getUsuarioByUsername(dto.clienteUsername());
        Servico servico = getServicoById(dto.servicoId());

        validator.validarCriacao(dto, barbeiro, cliente, servico);
        horarioValidator.validarDisponibilidade(barbeiro, dto.data(), dto.hora(), servico, null);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setData(dto.data());
        agendamento.setHora(dto.hora());

        return AgendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    // apagar e cancelar

    public void delete(Long id){
        Agendamento agendamento = getAgendamentoById(id);
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO){
            throw new AgendamentoStatusInvalidoException("Agendamento já foi aceito pelo barbeiro não e possivel apagar apenas cancelar");
        }
        agendamentoRepository.delete(agendamento);
    }

    public void cancelar(Long id){
        Agendamento agendamento = getAgendamentoById(id);
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.AGENDADO){
            throw new AgendamentoStatusInvalidoException("Agendamento não foi aceito pelo barbeiro não ou já foi cancelado");
        }
        agendamento.setAgendamentoStatus(AgendamentoStatus.CANCELADO);
        agendamentoRepository.save(agendamento);
    }

    // Update

    public void update(Long id, AgendamentoUpdateDTO dto) {

        Agendamento agendamento = getAgendamentoById(id);
        validator.validarAtualizacao(agendamento);

        LocalDate novaData = dto.data() != null ? dto.data() : agendamento.getData();
        LocalTime novaHora = dto.hora() != null ? dto.hora() : agendamento.getHora();

        validator.validarDataEHora(novaData, novaHora, agendamento.getServico().getDuracaoMediaEmMinutos());
        horarioValidator.validarDisponibilidade(
                agendamento.getBarbeiro(),
                novaData,
                novaHora,
                agendamento.getServico(),
                agendamento.getId()
        );

        agendamento.setData(novaData);
        agendamento.setHora(novaHora);

        if (dto.agendamentoStatus() != null) {
            agendamento.setAgendamentoStatus(dto.agendamentoStatus());
        }

        agendamentoRepository.save(agendamento);
    }

    // Metodos de manipulação objetivos

    @Transactional
    public void finalizar(Long id) {

        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Agendamento agendamento = getAgendamentoById(id);

        validator.validarFinalizacao(usuario, agendamento);

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioPermitido = LocalDateTime
                .of(agendamento.getData(), agendamento.getHora())
                .plusMinutes(10);

        if (agora.isBefore(inicioPermitido)) {
            throw new AgendamentoHorarioInvalidoException(
                    "O agendamento só pode ser finalizado 10 minutos após o horário marcado"
            );
        }

        agendamento.setAgendamentoStatus(AgendamentoStatus.CONCLUIDO);
        agendamentoRepository.save(agendamento);
    }

    public void aceitar(Long id) {
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Agendamento agendamento = getAgendamentoById(id);

        // autorização OK
        if (!usuario.role().equals(UserRole.BARBEIRO.getRole())
                && !usuario.role().equals(UserRole.ADMIN.getRole())) {
            throw new UsuarioNaoBarbeiroException();
        }

        // STATUS GATE (o que você perdeu)
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO) {
            throw new AgendamentoStatusInvalidoException("Só pedidos REQUISITADOS podem ser aceitos");
        }


        agendamento.setAgendamentoStatus(AgendamentoStatus.AGENDADO);
        agendamentoRepository.save(agendamento);
    }



    // Finds e gets

    public AgendamentoResponseDTO findById(Long id){
        return AgendamentoMapper.toResponse(agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNotFoundException::new));
    }


    public List<AgendamentoResponseDTO> find(
            String clienteUserName,
            String barbeiroUserName,
            Long servicoId,
            LocalDate data,
            LocalTime hora,
            AgendamentoStatus status
    ) {
        return agendamentoRepository.findAll(
                AgendamentoSpecification.filtro(
                        clienteUserName,
                        barbeiroUserName,
                        servicoId,
                        data,
                        hora,
                        status
                )
        ).stream().map(AgendamentoMapper::toResponse).toList();
    }

    public List<AgendamentoResponseDTO> getAutenticado(){
        UsuarioResponseDTO usuarioResponseDTO = usuarioService.getUsuarioAutenticado();
        List<Agendamento> agendamentos = new ArrayList<>();
        agendamentos.addAll(agendamentoRepository.findByBarbeiro_Username(usuarioResponseDTO.username()));
        agendamentos.addAll(agendamentoRepository.findByCliente_Username(usuarioResponseDTO.username()));
        return AgendamentoMapper.toResponses(agendamentos);

    }

    // Metodos privados de get

    private Usuario getUsuarioByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(UsuarioNotFoundException::new);
    }

    private Servico getServicoById(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(ServicoNotFoundException::new);
    }

    private Agendamento getAgendamentoById(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNotFoundException::new);
    }

}