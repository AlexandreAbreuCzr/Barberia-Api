package com.alexandre.Barbearia_Api.infra.exceptions.agendamento;

public class AgendamentoNaoPermitidoAoDomingoException extends RuntimeException {
    public AgendamentoNaoPermitidoAoDomingoException(String detalhes) {
        super(detalhes);
    }

    public AgendamentoNaoPermitidoAoDomingoException() {
        super("Agendamentos não são permitidos aos domingos");
    }
}