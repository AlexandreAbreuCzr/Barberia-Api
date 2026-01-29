package com.alexandre.Barbearia_Api.infra.exceptions.usuario;

public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(String detalhes) {
        super(detalhes);
    }

    public UsuarioNotFoundException() {
        super("Usuário não encontrado");
    }
}
