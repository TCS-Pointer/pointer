package br.com.pointer.pointer_back.exception;

public class SenhaInvalidaException extends RuntimeException {
    public SenhaInvalidaException(String message) {
        super(message);
    }

    public SenhaInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
} 