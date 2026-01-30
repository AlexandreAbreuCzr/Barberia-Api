package com.alexandre.Barbearia_Api.infra.exceptions.agendamento;

public class AgendamentoStatusInvalidoException extends RuntimeException {
    public AgendamentoStatusInvalidoException(String detalhes) {
        super(detalhes);
    }

    public AgendamentoStatusInvalidoException() {
        super("Agendamento já foi aceito pelo barbeiro");
    }
}