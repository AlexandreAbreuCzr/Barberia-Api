package com.alexandre.Barbearia_Api.infra.exceptions.usuario;

public class PasswordResetCodeInvalidException extends RuntimeException {
    public PasswordResetCodeInvalidException(String detalhes) {
        super(detalhes);
    }

    public PasswordResetCodeInvalidException() {
        super("Código inválido ou expirado");
    }
}
