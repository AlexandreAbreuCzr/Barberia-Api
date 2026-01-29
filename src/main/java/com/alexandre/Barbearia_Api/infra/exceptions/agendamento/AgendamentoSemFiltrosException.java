package com.alexandre.Barbearia_Api.infra.exceptions.agendamento;

public class AgendamentoSemFiltrosException extends RuntimeException {
    public AgendamentoSemFiltrosException(String detalhes) {
        super(detalhes);
    }

    public AgendamentoSemFiltrosException() {
        super("Informe ao menos um filtro para busca de agendamentos");
    }
}