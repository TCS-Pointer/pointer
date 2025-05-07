package br.com.pointer.pointer_back.exception;

public class EmailInvalidoException extends RuntimeException {
    public EmailInvalidoException(String message) {
        super(message);
    }

    public EmailInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
} 