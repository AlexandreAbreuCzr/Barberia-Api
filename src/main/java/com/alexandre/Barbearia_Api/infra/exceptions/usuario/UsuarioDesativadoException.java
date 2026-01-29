package com.alexandre.Barbearia_Api.infra.exceptions.usuario;

public class UsuarioDesativadoException extends RuntimeException {

    public UsuarioDesativadoException(String detalhes) {
        super(detalhes);
    }

    public UsuarioDesativadoException() {
        super("Usuário desativado");
    }
}
