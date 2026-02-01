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
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class AgendamentoValidator {


    public void validarCriacao(
            AgendamentoCreateDTO dto,
            Usuario barbeiro,
            Usuario cliente,
            Servico servico
    ) {
        validarUsuarios(barbeiro, cliente);
        validarServico(servico);
        validarBarbeiro(barbeiro);
        validarDataEHora(dto.data(), dto.hora());
    }

    public void validarAtualizacao(Agendamento agendamento) {
        if (agendamento.getAgendamentoStatus() != AgendamentoStatus.REQUISITADO) {
            throw new AgendamentoStatusInvalidoException(
                    "Agendamento não pode mais ser modificado"
            );
        }
    }

    public void validarFinalizacao(
            UsuarioResponseDTO usuario,
            Agendamento agendamento
    ) {
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

    public void validarDataEHora(LocalDate data, LocalTime hora) {
        validarData(data);
        validarHora(hora);
    }

    // Metodos privados

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

    // Data e hora

    private void validarHora(LocalTime hora) {
        boolean manha = !hora.isBefore(LocalTime.of(9, 0)) && hora.isBefore(LocalTime.of(12, 0));
        boolean tarde = !hora.isBefore(LocalTime.of(13, 0)) && hora.isBefore(LocalTime.of(20, 0));

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
}