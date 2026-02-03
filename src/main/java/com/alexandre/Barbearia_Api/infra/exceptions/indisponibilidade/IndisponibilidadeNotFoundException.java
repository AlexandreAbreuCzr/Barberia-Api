package com.alexandre.Barbearia_Api.infra.exceptions.indisponibilidade;

public class IndisponibilidadeNotFoundException extends RuntimeException {

    public IndisponibilidadeNotFoundException(String detalhes) {
        super(detalhes);
    }

    public IndisponibilidadeNotFoundException() {
        super("Indisponibilidade não encontrada");
    }
}
