package br.com.pointer.pointer_back.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailInvalidoExceptionTest {

    @Test
    void constructor_ComMensagem_DeveCriarExcecaoComMensagem() {
        // Arrange
        String mensagem = "Email inválido";

        // Act
        EmailInvalidoException ex = new EmailInvalidoException(mensagem);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_ComMensagemECausa_DeveCriarExcecaoComMensagemECausa() {
        // Arrange
        String mensagem = "Email inválido";
        Throwable causa = new RuntimeException("Causa");

        // Act
        EmailInvalidoException ex = new EmailInvalidoException(mensagem, causa);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertEquals(causa, ex.getCause());
    }
} 