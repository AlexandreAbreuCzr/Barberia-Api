package com.alexandre.Barbearia_Api.infra.exceptions.agendamento;

public class AgendamentoNotFoundException extends RuntimeException {
    public AgendamentoNotFoundException(String detalhes) {
        super(detalhes);
    }

    public AgendamentoNotFoundException() {
        super("Agendamento não encontrado");
    }
}