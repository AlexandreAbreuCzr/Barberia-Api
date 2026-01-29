package com.alexandre.Barbearia_Api.infra.exceptions.usuario;

public class UsuarioJaExisteException extends RuntimeException {

    public UsuarioJaExisteException(String detalhes) {
        super(detalhes);
    }

    public UsuarioJaExisteException() {
        super("Usuário já existente");
    }
}
