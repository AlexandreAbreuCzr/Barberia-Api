package com.alexandre.Barbearia_Api.service.agendamento;

import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoCreateDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoResponseDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.disponibilidade.AgendamentoDisponibilidadeDiaDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.disponibilidade.AgendamentoDisponibilidadeResponseDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.mapper.AgendamentoMapper;
import com.alexandre.Barbearia_Api.dto.agendamento.update.AgendamentoUpdateDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.*;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNaoBarbeiroException;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoDesativadoException;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoNotFoundException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.*;
import com.alexandre.Barbearia_Api.repository.AgendamentoRepository;
import com.alexandre.Barbearia_Api.repository.IndisponibilidadeRepository;
import com.alexandre.Barbearia_Api.repository.ServicoRepository;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import com.alexandre.Barbearia_Api.service.caixa.CaixaService;
import com.alexandre.Barbearia_Api.service.comissao.ComissaoService;
import com.alexandre.Barbearia_Api.service.usuario.UsuarioService;
import com.alexandre.Barbearia_Api.specificifications.AgendamentoSpecification;
import com.alexandre.Barbearia_Api.specificifications.IndisponibilidadeSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    private final UsuarioService usuarioService;
    private final AgendamentoRepository agendamentoRepository;
    private final IndisponibilidadeRepository indisponibilidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoValidator validator;
    private final AgendamentoHorarioValidator horarioValidator;
    private final ComissaoService comissaoService;
    private final CaixaService caixaService;

    // Injeção

    public AgendamentoService(
            UsuarioService usuarioService,
            AgendamentoRepository agendamentoRepository,
            IndisponibilidadeRepository indisponibilidadeRepository,
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository,
            AgendamentoValidator validator,
            AgendamentoHorarioValidator horarioValidator,
            ComissaoService comissaoService,
            CaixaService caixaService
    ) {
        this.usuarioService = usuarioService;
        this.agendamentoRepository = agendamentoRepository;
        this.indisponibilidadeRepository = indisponibilidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.validator = validator;
        this.horarioValidator = horarioValidator;
        this.comissaoService = comissaoService;
        this.caixaService = caixaService;
    }

    // Criador de agendamento

    public AgendamentoResponseDTO create(AgendamentoCreateDTO dto) {
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        if (!canManageAgenda(usuario) && (dto.clienteUsername() == null
                || !dto.clienteUsername().equalsIgnoreCase(usuario.username()))) {
            throw new AccessDeniedException("Você não pode criar agendamento para outro usuário.");
        }

        Usuario barbeiro = null;
        if (dto.barbeiroUsername() != null && !dto.barbeiroUsername().isBlank()) {
            barbeiro = getUsuarioByUsername(dto.barbeiroUsername());
        }
        Usuario cliente = getUsuarioByUsername(dto.clienteUsername());
        Servico servico = getServicoById(dto.servicoId());

        validator.validarCriacao(dto, barbeiro, cliente, servico);
        if (barbeiro != null) {
            horarioValidator.validarDisponibilidade(barbeiro, dto.data(), dto.hora(), servico, null);
        }

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
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Agendamento agendamento = getAgendamentoById(id);
        if (!canManageAgenda(usuario) && !isCliente(agendamento, usuario.username())) {
            throw new AccessDeniedException("Você não pode excluir este agendamento.");
        }
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO){
            throw new AgendamentoStatusInvalidoException(
                    "Agendamento já foi aceito pelo barbeiro; não é possível apagar, apenas cancelar."
            );
        }
        agendamentoRepository.delete(agendamento);
    }

    public void cancelar(Long id){
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Agendamento agendamento = getAgendamentoById(id);
        if (!canManageAgenda(usuario)
                && !isCliente(agendamento, usuario.username())
                && !isBarbeiroDoAgendamento(agendamento, usuario.username())) {
            throw new AccessDeniedException("Você não pode cancelar este agendamento.");
        }
        AgendamentoStatus status = agendamento.getAgendamentoStatus();
        if (status != AgendamentoStatus.REQUISITADO && status != AgendamentoStatus.AGENDADO){
            throw new AgendamentoStatusInvalidoException("Agendamento não pode ser cancelado neste status");
        }
        agendamento.setAgendamentoStatus(AgendamentoStatus.CANCELADO);
        agendamentoRepository.save(agendamento);
    }

    // Update

    public void update(Long id, AgendamentoUpdateDTO dto) {

        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Agendamento agendamento = getAgendamentoById(id);
        if (!canManageAgenda(usuario) && !isCliente(agendamento, usuario.username())) {
            throw new AccessDeniedException("Você não pode alterar este agendamento.");
        }
        validator.validarAtualizacao(agendamento);

        LocalDate novaData = dto.data() != null ? dto.data() : agendamento.getData();
        LocalTime novaHora = dto.hora() != null ? dto.hora() : agendamento.getHora();

        validator.validarDataEHora(novaData, novaHora, agendamento.getServico().getDuracaoMediaEmMinutos());
        Usuario barbeiro = agendamento.getBarbeiro();
        if (barbeiro != null) {
            validator.validarIndisponibilidade(barbeiro, agendamento.getServico(), novaData, novaHora);
            horarioValidator.validarDisponibilidade(
                    barbeiro,
                    novaData,
                    novaHora,
                    agendamento.getServico(),
                    agendamento.getId()
            );
        }

        agendamento.setData(novaData);
        agendamento.setHora(novaHora);

        if (dto.agendamentoStatus() != null) {
            agendamento.setAgendamentoStatus(dto.agendamentoStatus());
        } else if (agendamento.getAgendamentoStatus() == AgendamentoStatus.CANCELADO) {
            agendamento.setAgendamentoStatus(AgendamentoStatus.REQUISITADO);
        }

        agendamentoRepository.save(agendamento);
    }

    // Métodos de manipulação objetivos

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
        comissaoService.createForAgendamento(agendamento);
        caixaService.createEntradaAgendamento(agendamento);
    }

    @Transactional
    public void aceitar(Long id) {
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Agendamento agendamento = getAgendamentoById(id);

        // autorização OK
        if (!canAcceptAppointment(usuario)) {
            throw new UsuarioNaoBarbeiroException();
        }

        // STATUS GATE (o que você perdeu)
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO) {
            throw new AgendamentoStatusInvalidoException("Só pedidos REQUISITADOS podem ser aceitos");
        }

        Usuario barbeiro = getUsuarioByUsername(usuario.username());
        if (agendamento.getBarbeiro() == null) {
            validator.validarBarbeiro(barbeiro);
            validator.validarIndisponibilidade(
                    barbeiro,
                    agendamento.getServico(),
                    agendamento.getData(),
                    agendamento.getHora()
            );
            horarioValidator.validarDisponibilidade(
                    barbeiro,
                    agendamento.getData(),
                    agendamento.getHora(),
                    agendamento.getServico(),
                    null
            );
            agendamento.setBarbeiro(barbeiro);
        } else if (!agendamento.getBarbeiro().getUsername().equalsIgnoreCase(usuario.username())
                && !hasRole(usuario, UserRole.ADMIN)) {
            throw new UsuarioNaoBarbeiroException();
        }

        agendamento.setAgendamentoStatus(AgendamentoStatus.AGENDADO);
        agendamentoRepository.save(agendamento);
    }



    // Finds e gets

    public AgendamentoResponseDTO findById(Long id){
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNotFoundException::new);

        if (!canManageAgenda(usuario)
                && !isCliente(agendamento, usuario.username())
                && !isBarbeiroDoAgendamento(agendamento, usuario.username())) {
            throw new AccessDeniedException("Você não pode acessar este agendamento.");
        }

        return AgendamentoMapper.toResponse(agendamento);
    }


    public List<AgendamentoResponseDTO> find(
            String clienteUserName,
            String barbeiroUserName,
            Long servicoId,
            LocalDate data,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime hora,
            AgendamentoStatus status,
            Boolean semBarbeiro
    ) {
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        String clienteFiltro = clienteUserName;
        String barbeiroFiltro = barbeiroUserName;
        Boolean semBarbeiroFiltro = semBarbeiro;

        if (canManageAgenda(usuario)) {
            // sem restrições adicionais
        } else if (isBarbeiro(usuario)) {
            if (barbeiroFiltro != null && !barbeiroFiltro.equalsIgnoreCase(usuario.username())) {
                throw new AccessDeniedException("Você não pode consultar agendamentos de outro barbeiro.");
            }
            if (barbeiroFiltro == null && semBarbeiroFiltro == null) {
                barbeiroFiltro = usuario.username();
            }
        } else {
            clienteFiltro = usuario.username();
            barbeiroFiltro = null;
            semBarbeiroFiltro = null;
        }

        return agendamentoRepository.findAll(
                AgendamentoSpecification.filtro(
                        clienteFiltro,
                        barbeiroFiltro,
                        servicoId,
                        data,
                        dataInicio,
                        dataFim,
                        hora,
                        status,
                        semBarbeiroFiltro
                )
        ).stream().map(AgendamentoMapper::toResponse).toList();
    }

    public AgendamentoDisponibilidadeResponseDTO getDisponibilidade(
            String barbeiroUserName,
            Long servicoId,
            LocalDate inicio,
            LocalDate fim
    ) {
        validarPeriodoDisponibilidade(inicio, fim);

        Usuario barbeiro = getUsuarioByUsername(barbeiroUserName);
        validator.validarBarbeiro(barbeiro);

        Servico servico = getServicoById(servicoId);
        if (!servico.isStatus()) {
            throw new ServicoDesativadoException();
        }

        int duracaoEmMinutos = getDuracaoMinutos(servico);
        LocalDateTime antecedenciaMinima = LocalDateTime.now().plusMinutes(15);

        List<Agendamento> conflitos = agendamentoRepository
                .findByBarbeiro_UsernameAndDataBetweenOrderByDataAscHoraAsc(
                        barbeiroUserName,
                        inicio,
                        fim
                )
                .stream()
                .filter(agendamento ->
                        agendamento.getAgendamentoStatus() == AgendamentoStatus.REQUISITADO
                                || agendamento.getAgendamentoStatus() == AgendamentoStatus.AGENDADO
                )
                .collect(Collectors.toList());

        LocalDateTime inicioPeriodo = inicio.atStartOfDay();
        LocalDateTime fimPeriodo = fim.plusDays(1).atStartOfDay().minusNanos(1);
        Specification<Indisponibilidade> indisponibilidadeSpec = Specification.<Indisponibilidade>unrestricted()
                .and(IndisponibilidadeSpecification.barbeiroUsername(barbeiroUserName))
                .and(IndisponibilidadeSpecification.overlap(inicioPeriodo, fimPeriodo));
        List<Indisponibilidade> indisponibilidades = indisponibilidadeRepository.findAll(indisponibilidadeSpec);

        List<AgendamentoDisponibilidadeDiaDTO> dias = new ArrayList<>();
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            List<LocalTime> horarios = calcularHorariosDisponiveis(
                    data,
                    duracaoEmMinutos,
                    conflitos,
                    indisponibilidades,
                    antecedenciaMinima
            );
            dias.add(new AgendamentoDisponibilidadeDiaDTO(data, !horarios.isEmpty(), horarios));
        }

        return new AgendamentoDisponibilidadeResponseDTO(
                barbeiroUserName,
                servicoId,
                duracaoEmMinutos,
                inicio,
                fim,
                dias
        );
    }

    public List<AgendamentoResponseDTO> getAutenticado(){
        UsuarioResponseDTO usuarioResponseDTO = usuarioService.getUsuarioAutenticado();
        List<Agendamento> agendamentos = new ArrayList<>();
        agendamentos.addAll(agendamentoRepository.findByBarbeiro_Username(usuarioResponseDTO.username()));
        agendamentos.addAll(agendamentoRepository.findByCliente_Username(usuarioResponseDTO.username()));
        return AgendamentoMapper.toResponses(agendamentos);

    }

    // Métodos privados de get

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

    private boolean hasRole(UsuarioResponseDTO usuario, UserRole... roles) {
        UserRole role = UserRole.from(usuario != null ? usuario.role() : null);
        if (role == null || roles == null || roles.length == 0) return false;

        for (UserRole allowed : roles) {
            if (allowed == role) return true;
        }
        return false;
    }

    private boolean canManageAgenda(UsuarioResponseDTO usuario) {
        return hasRole(usuario, UserRole.ADMIN, UserRole.GERENTE, UserRole.RECEPCIONISTA);
    }

    private boolean canAcceptAppointment(UsuarioResponseDTO usuario) {
        return hasRole(usuario, UserRole.BARBEIRO, UserRole.ADMIN);
    }

    private boolean isBarbeiro(UsuarioResponseDTO usuario) {
        return hasRole(usuario, UserRole.BARBEIRO);
    }

    private boolean isCliente(Agendamento agendamento, String username) {
        return agendamento.getCliente() != null
                && agendamento.getCliente().getUsername().equalsIgnoreCase(username);
    }

    private boolean isBarbeiroDoAgendamento(Agendamento agendamento, String username) {
        return agendamento.getBarbeiro() != null
                && agendamento.getBarbeiro().getUsername().equalsIgnoreCase(username);
    }

    private void validarPeriodoDisponibilidade(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Informe inicio e fim para consultar disponibilidade.");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data fim deve ser igual ou posterior a data inicio.");
        }

        long dias = Duration.between(
                inicio.atStartOfDay(),
                fim.plusDays(1).atStartOfDay()
        ).toDays();
        if (dias > 62) {
            throw new IllegalArgumentException("A consulta de disponibilidade permite no maximo 62 dias.");
        }
    }

    private List<LocalTime> calcularHorariosDisponiveis(
            LocalDate data,
            int duracaoEmMinutos,
            List<Agendamento> conflitos,
            List<Indisponibilidade> indisponibilidades,
            LocalDateTime antecedenciaMinima
    ) {
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        List<LocalTime> horarios = new ArrayList<>();
        adicionarHorariosNoTurno(
                horarios,
                data,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                duracaoEmMinutos,
                conflitos,
                indisponibilidades,
                antecedenciaMinima
        );
        adicionarHorariosNoTurno(
                horarios,
                data,
                LocalTime.of(13, 0),
                LocalTime.of(20, 0),
                duracaoEmMinutos,
                conflitos,
                indisponibilidades,
                antecedenciaMinima
        );
        return horarios;
    }

    private void adicionarHorariosNoTurno(
            List<LocalTime> horarios,
            LocalDate data,
            LocalTime inicioTurno,
            LocalTime fimTurno,
            int duracaoEmMinutos,
            List<Agendamento> conflitos,
            List<Indisponibilidade> indisponibilidades,
            LocalDateTime antecedenciaMinima
    ) {
        LocalTime inicio = inicioTurno;
        while (!inicio.plusMinutes(duracaoEmMinutos).isAfter(fimTurno)) {
            LocalDateTime inicioDataHora = LocalDateTime.of(data, inicio);
            LocalTime fim = inicio.plusMinutes(duracaoEmMinutos);

            boolean horarioValido = !inicioDataHora.isBefore(antecedenciaMinima)
                    && !possuiConflitoComAgendamento(data, inicio, fim, conflitos)
                    && !possuiConflitoComIndisponibilidade(data, inicio, fim, indisponibilidades);

            if (horarioValido) {
                horarios.add(inicio);
            }

            inicio = inicio.plusMinutes(15);
        }
    }

    private boolean possuiConflitoComAgendamento(
            LocalDate data,
            LocalTime inicio,
            LocalTime fim,
            List<Agendamento> conflitos
    ) {
        for (Agendamento agendamento : conflitos) {
            if (!data.equals(agendamento.getData())) {
                continue;
            }

            LocalTime inicioExistente = agendamento.getHora();
            LocalTime fimExistente = inicioExistente.plusMinutes(getDuracaoMinutos(agendamento.getServico()));
            boolean conflito = inicio.isBefore(fimExistente) && fim.isAfter(inicioExistente);
            if (conflito) {
                return true;
            }
        }
        return false;
    }

    private boolean possuiConflitoComIndisponibilidade(
            LocalDate data,
            LocalTime inicio,
            LocalTime fim,
            List<Indisponibilidade> indisponibilidades
    ) {
        LocalDateTime inicioDataHora = LocalDateTime.of(data, inicio);
        LocalDateTime fimDataHora = LocalDateTime.of(data, fim);

        for (Indisponibilidade indisponibilidade : indisponibilidades) {
            boolean conflito = !indisponibilidade.getInicio().isAfter(fimDataHora)
                    && !indisponibilidade.getFim().isBefore(inicioDataHora);
            if (conflito) {
                return true;
            }
        }
        return false;
    }

    private int getDuracaoMinutos(Servico servico) {
        if (servico == null || servico.getDuracaoMediaEmMinutos() == null || servico.getDuracaoMediaEmMinutos() <= 0) {
            return 30;
        }
        return servico.getDuracaoMediaEmMinutos();
    }

}

