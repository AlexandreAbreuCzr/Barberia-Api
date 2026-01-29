package com.alexandre.Barbearia_Api.infra.exceptions.usuario;

public class UsuarioNaoBarbeiroException extends RuntimeException {
    public UsuarioNaoBarbeiroException(String detalhes) {
        super(detalhes);
    }

    public UsuarioNaoBarbeiroException() {
        super("O usuario não e um barbeiro");
    }
}