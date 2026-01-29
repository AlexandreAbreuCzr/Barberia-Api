package com.alexandre.Barbearia_Api.dto.exceptions;

public record ApiErrorDTO(
        int status,
        String error,
        String message
){}
