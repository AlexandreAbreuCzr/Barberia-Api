package com.alexandre.Barbearia_Api.infra;

import com.alexandre.Barbearia_Api.dto.exceptions.ApiErrorDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.agendamento.*;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoDesativadoException;
import com.alexandre.Barbearia_Api.infra.exceptions.servico.ServicoNotFoundException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioDesativadoException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioJaExisteException;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UsuarioJaExisteException.class)
    private ResponseEntity<ApiErrorDTO> usuarioJaExisteHandler(UsuarioJaExisteException exception){
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "CONFLICT",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    private ResponseEntity<ApiErrorDTO> usuarioNotFoundHandler(UsuarioNotFoundException exception){
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UsuarioDesativadoException.class)
    private ResponseEntity<ApiErrorDTO> usuarioDesativadoHandler(UsuarioDesativadoException exception){
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ServicoNotFoundException.class)
    private ResponseEntity<ApiErrorDTO> servicoNotFoundHandler(ServicoNotFoundException exception){
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ServicoDesativadoException.class)
    private ResponseEntity<ApiErrorDTO> servicoDesativadoHandler(ServicoDesativadoException exception){
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AgendamentoNotFoundException.class)
    private ResponseEntity<ApiErrorDTO> agendamentoNotFoundHandler(AgendamentoNotFoundException exception){
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AgendamentoOcupadoException.class)
    private ResponseEntity<ApiErrorDTO> agendamentoOcupadoHandler(AgendamentoOcupadoException exception){
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AgendamentoAceitoBarbeiroException.class)
    private ResponseEntity<ApiErrorDTO> agendamentoAceitoHandler(
            AgendamentoAceitoBarbeiroException exception
    ) {
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AgendamentoHorarioInvalidoException.class)
    private ResponseEntity<ApiErrorDTO> agendamentoHorarioInvalidoHandler(AgendamentoHorarioInvalidoException exception
    ) {
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AgendamentoNaoPermitidoAoDomingoException.class)
    private ResponseEntity<ApiErrorDTO> agendamentoDomingoHandler(AgendamentoNaoPermitidoAoDomingoException exception
    ) {
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AgendamentoSemFiltrosException.class)
    private ResponseEntity<ApiErrorDTO> agendamentoSemFiltrosHandler(AgendamentoSemFiltrosException exception
    ) {
        ApiErrorDTO error = new ApiErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
