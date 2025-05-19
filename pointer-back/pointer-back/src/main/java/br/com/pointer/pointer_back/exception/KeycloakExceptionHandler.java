package br.com.pointer.pointer_back.exception;

import br.com.pointer.pointer_back.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class KeycloakExceptionHandler {

    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ApiResponse<Void>> handleKeycloakException(KeycloakException ex) {
        return ResponseEntity.ok(new ApiResponse<Void>().badRequest(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsuarioJaExisteException(UsuarioJaExisteException ex) {
        return ResponseEntity.ok(new ApiResponse<Void>().conflict(ex.getMessage(), null));
    }

    @ExceptionHandler(EmailInvalidoException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailInvalidoException(EmailInvalidoException ex) {
        return ResponseEntity.ok(new ApiResponse<Void>().unprocessableEntity(ex.getMessage()));
    }

    @ExceptionHandler(SenhaInvalidaException.class)
    public ResponseEntity<ApiResponse<Void>> handleSenhaInvalidaException(SenhaInvalidaException ex) {
        return ResponseEntity.ok(new ApiResponse<Void>().unprocessableEntity(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsuarioNaoEncontradoException(UsuarioNaoEncontradoException ex) {
        return ResponseEntity.ok(new ApiResponse<Void>().notFound(ex.getMessage()));
    }
} 