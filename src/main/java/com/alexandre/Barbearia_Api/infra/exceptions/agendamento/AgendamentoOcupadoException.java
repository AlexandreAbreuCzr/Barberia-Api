package com.alexandre.Barbearia_Api.infra.exceptions.agendamento;

public class AgendamentoOcupadoException extends RuntimeException {
    public AgendamentoOcupadoException(String detalhes) {
        super(detalhes);
    }

    public AgendamentoOcupadoException() {
        super("Agendamento ocupado");
    }
}