package com.alexandre.Barbearia_Api.infra.exceptions.agendamento;

public class AgendamentoHorarioInvalidoException extends RuntimeException {
    public AgendamentoHorarioInvalidoException(String detalhes) {
        super(detalhes);
    }

    public AgendamentoHorarioInvalidoException() {
        super("Horário inválido. Atendimento das 09:00 às 12:00 e das 13:00 às 20:00");
    }
}