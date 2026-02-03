package com.alexandre.Barbearia_Api.infra.exceptions.indisponibilidade;

public class IndisponibilidadeNotFoundInicioFimException extends RuntimeException {

    public IndisponibilidadeNotFoundInicioFimException(String detalhes) {
        super(detalhes);
    }

    public IndisponibilidadeNotFoundInicioFimException() {
        super("Informe inicio e fim juntos");
    }
}
