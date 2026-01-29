package com.alexandre.Barbearia_Api.infra.exceptions.servico;

public class ServicoNotFoundException extends RuntimeException {

    public ServicoNotFoundException(String detalhes) {
        super(detalhes);
    }

    public ServicoNotFoundException() {
        super("Servico não encontrado");
    }
}
