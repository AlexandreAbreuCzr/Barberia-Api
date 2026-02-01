package com.alexandre.Barbearia_Api.service.agendamento;

import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.AgendamentoHorarioInvalidoException;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.AgendamentoOcupadoException;
import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.Servico;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.repository.AgendamentoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class AgendamentoHorarioValidator {

    private final AgendamentoRepository repository;

    public AgendamentoHorarioValidator(AgendamentoRepository repository) {
        this.repository = repository;
    }

    public void validarDisponibilidade(
            Usuario barbeiro,
            LocalDate data,
            LocalTime hora,
            Servico servico,
            Long ignorarAgendamentoId
    ) {
        LocalTime inicioNovo = hora;
        LocalTime fimNovo = hora.plusMinutes(servico.getDuracaoMediaEmMinutos());

        if (fimNovo.isAfter(LocalTime.of(20, 0))) {
            throw new AgendamentoHorarioInvalidoException(
                    "Serviço ultrapassa o horário de funcionamento"
            );
        }

        List<Agendamento> agendamentos = repository.findByBarbeiroAndData(barbeiro, data);

        for (Agendamento agendamento : agendamentos) {

            if (ignorarAgendamentoId != null &&
                    agendamento.getId().equals(ignorarAgendamentoId)) {
                continue;
            }

            LocalTime inicioExistente = agendamento.getHora();
            LocalTime fimExistente = inicioExistente.plusMinutes(
                    agendamento.getServico().getDuracaoMediaEmMinutos()
            );

            boolean conflito =
                    inicioNovo.isBefore(fimExistente)
                            && fimNovo.isAfter(inicioExistente);

            if (conflito) {
                throw new AgendamentoOcupadoException();
            }
        }
    }
}