package com.alexandre.Barbearia_Api.infra.exceptions.agendamento;

public class AgendamentoAceitoBarbeiroException extends RuntimeException {
    public AgendamentoAceitoBarbeiroException(String detalhes) {
        super(detalhes);
    }

    public AgendamentoAceitoBarbeiroException() {
        super("Agendamento já foi aceito pelo barbeiro");
    }
}