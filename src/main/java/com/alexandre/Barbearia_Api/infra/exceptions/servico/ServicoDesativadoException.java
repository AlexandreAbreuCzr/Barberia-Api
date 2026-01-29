package com.alexandre.Barbearia_Api.infra.exceptions.servico;

public class ServicoDesativadoException extends RuntimeException {

    public ServicoDesativadoException(String detalhes) {
        super(detalhes);
    }

    public ServicoDesativadoException() {
        super("Servico desativado");
    }
}
