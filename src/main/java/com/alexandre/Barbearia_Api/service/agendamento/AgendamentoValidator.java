package com.alexandre.Barbearia_Api.service.agendamento;

import com.alexandre.Barbearia_Api.dto.agendamento.AgendamentoCreateDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.AgendamentoHorarioInvalidoException;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.AgendamentoNaoPermitidoAoDomingoException;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.AgendamentoStatusInvalidoException;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoDesativadoException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioDesativadoException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNaoBarbeiroException;
import com.alexandre.Barbearia_Api.model.*;
import com.alexandre.Barbearia_Api.repository.IndisponibilidadeRepository;
import com.alexandre.Barbearia_Api.specificifications.IndisponibilidadeSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class AgendamentoValidator {

    private final IndisponibilidadeRepository indisponibilidadeRepository;

    public AgendamentoValidator(IndisponibilidadeRepository indisponibilidadeRepository) {
        this.indisponibilidadeRepository = indisponibilidadeRepository;
    }

    public void validarCriacao(
            AgendamentoCreateDTO dto,
            Usuario barbeiro,
            Usuario cliente,
            Servico servico
    ) {
        validarUsuarios(barbeiro, cliente);
        validarServico(servico);
        validarBarbeiro(barbeiro);

        // valida data e intervalo (inicio+duracao) dentro do funcionamento
        validarData(dto.data());
        validarIntervaloDentroDoFuncionamento(dto.data(), dto.hora(), servico.getDuracaoMediaEmMinutos());

        // valida conflito com indisponibilidade do barbeiro
        validarIndisponibilidade(dto, barbeiro, servico);
    }

    public void validarAtualizacao(Agendamento agendamento) {
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO) {
            throw new AgendamentoStatusInvalidoException("Agendamento não pode mais ser modificado");
        }
    }

    public void validarFinalizacao(UsuarioResponseDTO usuario, Agendamento agendamento) {
        boolean autorizado =
                usuario.username().equals(agendamento.getBarbeiro().getUsername())
                        || usuario.role().equals(UserRole.ADMIN.getRole());

        if (!autorizado) {
            throw new UsuarioNaoBarbeiroException();
        }

        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.AGENDADO) {
            throw new AgendamentoStatusInvalidoException(
                    "Somente agendamentos AGENDADOS podem ser finalizados"
            );
        }
    }

    // ======================
    //  Regras principais
    // ======================

    private void validarIndisponibilidade(AgendamentoCreateDTO dto, Usuario barbeiro, Servico servico) {
        LocalDateTime inicioAg = LocalDateTime.of(dto.data(), dto.hora());
        LocalDateTime fimAg = inicioAg.plusMinutes(servico.getDuracaoMediaEmMinutos());

        Specification<Indisponibilidade> spec = Specification.<Indisponibilidade>unrestricted()
                .and(IndisponibilidadeSpecification.barbeiroUsername(barbeiro.getUsername()))
                .and(IndisponibilidadeSpecification.overlap(inicioAg, fimAg));


        boolean existeConflito = indisponibilidadeRepository.exists(spec);

        if (existeConflito) {
            // Troque pela sua exception específica se tiver
            throw new AgendamentoHorarioInvalidoException(
                    "Horário indisponível para este barbeiro (conflito com indisponibilidade)."
            );
        }
    }

    /**
     * Regras de funcionamento:
     * - Manhã: 09:00 (inclusive) até 12:00 (exclusive)
     * - Almoço: 12:00 até 13:00 (bloqueado)
     * - Tarde: 13:00 (inclusive) até 20:00 (exclusive)
     *
     * IMPORTANTE: valida o INTERVALO do serviço, não só a hora de início.
     */
    private void validarIntervaloDentroDoFuncionamento(LocalDate data, LocalTime horaInicio, int duracaoMinutos) {
        LocalDateTime inicio = LocalDateTime.of(data, horaInicio);
        LocalDateTime fim = inicio.plusMinutes(duracaoMinutos);

        LocalDateTime manhaInicio = LocalDateTime.of(data, LocalTime.of(9, 0));
        LocalDateTime manhaFim = LocalDateTime.of(data, LocalTime.of(12, 0));

        LocalDateTime tardeInicio = LocalDateTime.of(data, LocalTime.of(13, 0));
        LocalDateTime tardeFim = LocalDateTime.of(data, LocalTime.of(20, 0));

        boolean cabeNaManha = !inicio.isBefore(manhaInicio) && fim.isBefore(manhaFim.plusNanos(1)); // fim <= 12:00
        boolean cabeNaTarde = !inicio.isBefore(tardeInicio) && fim.isBefore(tardeFim.plusNanos(1)); // fim <= 20:00

        if (!cabeNaManha && !cabeNaTarde) {
            throw new AgendamentoHorarioInvalidoException();
        }
    }

    public void validarDataEHora(LocalDate data, LocalTime hora, Integer duracaoMinutos) {
        validarData(data);
        validarIntervaloDentroDoFuncionamento(data, hora, duracaoMinutos);
    }


    // ======================
    //  Validações básicas
    // ======================

    private void validarUsuarios(Usuario barbeiro, Usuario cliente) {
        if (!barbeiro.isStatus() || !cliente.isStatus()) {
            throw new UsuarioDesativadoException();
        }
    }

    private void validarServico(Servico servico) {
        if (!servico.isStatus()) {
            throw new ServicoDesativadoException();
        }
    }

    private void validarBarbeiro(Usuario barbeiro) {
        if (barbeiro.getRole() != UserRole.BARBEIRO && barbeiro.getRole() != UserRole.ADMIN) {
            throw new UsuarioNaoBarbeiroException();
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
}
