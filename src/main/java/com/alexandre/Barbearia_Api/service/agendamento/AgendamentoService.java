package com.alexandre.Barbearia_Api.service.agendamento;

import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoCreateDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoResponseDTO;
import com.alexandre.Barbearia_Api.dto.agendamento.mapper.AgendamentoMapper;
import com.alexandre.Barbearia_Api.dto.agendamento.update.AgendamentoUpdateDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.*;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoDesativadoException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioDesativadoException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNaoBarbeiroException;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoNotFoundException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.*;
import com.alexandre.Barbearia_Api.repository.AgendamentoRepository;
import com.alexandre.Barbearia_Api.repository.ServicoRepository;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import com.alexandre.Barbearia_Api.service.usuario.UsuarioService;
import com.alexandre.Barbearia_Api.specificifications.AgendamentoSpecification;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgendamentoService {
    private final UsuarioService usuarioService;
    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;

    public AgendamentoService(UsuarioService usuarioService, AgendamentoRepository agendamentoRepository, UsuarioRepository usuarioRepository, ServicoRepository servicoRepository) {
        this.usuarioService = usuarioService;
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
    }

    public AgendamentoResponseDTO create(AgendamentoCreateDTO dto){

        validarDataEHora(dto.data(), dto.hora());

        Usuario barbeiro = getUsuarioByName(dto.barbeiroName());
        Usuario cliente = getUsuarioByName(dto.clienteName());
        Servico servico = getServicoById(dto.servicoId());

        if (!barbeiro.isStatus() || !cliente.isStatus()){
            throw new UsuarioDesativadoException();
        }

        if (!servico.isStatus()){
            throw new ServicoDesativadoException();
        }

        if (barbeiro.getRole() != UserRole.BARBEIRO && barbeiro.getRole() != UserRole.ADMIN){
            throw new UsuarioNaoBarbeiroException();
        }

        verificaBarbeiroOcupado(barbeiro, dto.data(), dto.hora(), servico, null);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setData(dto.data());
        agendamento.setHora(dto.hora());

        return AgendamentoMapper.toResponse(
                agendamentoRepository.save(agendamento)
        );
    }

    public void update(Long id, AgendamentoUpdateDTO dto) {
        Agendamento agendamento = getAgendamentoById(id);

        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO) {
            throw new AgendamentoAceitoBarbeiroException(
                    "Agendamento já foi aceito pelo barbeiro, não é possível modificar"
            );
        }

        LocalDate novaData = dto.data() != null
                ? dto.data()
                : agendamento.getData();

        LocalTime novaHora = dto.hora() != null
                ? dto.hora()
                : agendamento.getHora();


        validarDataEHora(novaData, novaHora);

        verificaBarbeiroOcupado(
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

    public void delete(Long id){
        Agendamento agendamento = getAgendamentoById(id);
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO){
            throw new AgendamentoAceitoBarbeiroException("Agendamento já foi aceito pelo barbeiro não e possivel apagar apenas cancelar");
        }
        agendamentoRepository.delete(agendamento);
    }

    public void cancelar(Long id){
        Agendamento agendamento = getAgendamentoById(id);
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.AGENDADO){
            throw new AgendamentoAceitoBarbeiroException("Agendamento não foi aceito pelo barbeiro não ou já foi cancelado");
        }
        agendamento.setAgendamentoStatus(AgendamentoStatus.CANCELADO);
        agendamentoRepository.save(agendamento);
    }

    private void verificaBarbeiroOcupado(
            Usuario barbeiro,
            LocalDate data,
            LocalTime hora,
            Servico servico,
            Long ignorarAgendamentoId
    ) {
        Integer duracaoMinutos = servico.getDuracaoMediaEmMinutos();

        LocalTime inicioNovo = hora;
        LocalTime fimNovo = hora.plusMinutes(duracaoMinutos);

        if (fimNovo.isAfter(LocalTime.of(20, 0))) {
            throw new AgendamentoHorarioInvalidoException(
                    "Serviço ultrapassa o horário de funcionamento"
            );
        }

        List<Agendamento> agendamentosDoBarbeiro =
                agendamentoRepository.findByBarbeiroAndData(barbeiro, data);

        for (Agendamento agendamento : agendamentosDoBarbeiro) {

            if (ignorarAgendamentoId != null &&
                    agendamento.getId().equals(ignorarAgendamentoId)) {
                continue;
            }

            LocalTime inicioExistente = agendamento.getHora();
            Integer duracaoExistente = agendamento.getServico().getDuracaoMediaEmMinutos();
            LocalTime fimExistente = inicioExistente.plusMinutes(duracaoExistente);

            boolean conflito =
                    inicioNovo.isBefore(fimExistente)
                            && fimNovo.isAfter(inicioExistente);

            if (conflito) {
                throw new AgendamentoOcupadoException();
            }
        }
    }

    public List<AgendamentoResponseDTO> find(
            String clienteName,
            String barbeiroName,
            Long servicoId,
            LocalDate data,
            LocalTime hora,
            AgendamentoStatus status
    ) {
        return agendamentoRepository.findAll(
                AgendamentoSpecification.filtro(
                        clienteName,
                        barbeiroName,
                        servicoId,
                        data,
                        hora,
                        status
                )
        ).stream().map(AgendamentoMapper::toResponse).toList();
    }


    public AgendamentoResponseDTO findById(Long id){
        return AgendamentoMapper.toResponse(agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNotFoundException::new));
    }

    public List<AgendamentoResponseDTO> getAutenticado(){
        UsuarioResponseDTO usuarioResponseDTO = usuarioService.getUsuarioAutenticado();
        List<Agendamento> agendamentos = new ArrayList<>();
        agendamentos.addAll(agendamentoRepository.findByBarbeiro_Name(usuarioResponseDTO.name()));
        agendamentos.addAll(agendamentoRepository.findByCliente_Name(usuarioResponseDTO.name()));
        return mapAgendamentos(agendamentos);

    }

    public void aceitar(Long id){
        AgendamentoUpdateDTO dto = new AgendamentoUpdateDTO(null, null, AgendamentoStatus.AGENDADO);
        update(id, dto);
    }

    private Usuario getUsuarioByName(String name){
        return usuarioRepository.findByName(name)
                .orElseThrow(UsuarioNotFoundException::new);
    }

    private Servico getServicoById(Long id){
        return servicoRepository.findById(id)
                .orElseThrow(ServicoNotFoundException::new);
    }

    private List<AgendamentoResponseDTO> mapAgendamentos(List<Agendamento> agendamentos){
        return agendamentos.stream().map(AgendamentoMapper::toResponse).toList();
    }

    private void validarDataEHora(LocalDate data, LocalTime hora) {
        validarData(data);
        validarHora(hora);
    }

    private void validarHora(LocalTime hora){
        boolean manha = !hora.isBefore(LocalTime.of(9, 0)) &&
                hora.isBefore(LocalTime.of(12, 0));

        boolean tarde = !hora.isBefore(LocalTime.of(13, 0)) &&
                hora.isBefore(LocalTime.of(20, 0));

        if (!manha && !tarde) {
            throw new AgendamentoHorarioInvalidoException();
        }
    }

    private void validarData(LocalDate data) {

        LocalDate hoje = LocalDate.now();

        if (!data.isAfter(hoje)) {
            throw new AgendamentoHorarioInvalidoException(
                    "Agendamentos devem ser feitos com pelo menos 1 dia de antecedência"
            );
        }

        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new AgendamentoNaoPermitidoAoDomingoException();
        }
    }

    private Agendamento getAgendamentoById(Long id){
        return agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNotFoundException::new);
    }

}
