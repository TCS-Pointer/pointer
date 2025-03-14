package br.com.pointer.pointer_back.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SenhaInvalidaExceptionTest {

    @Test
    void constructor_ComMensagem_DeveCriarExcecaoComMensagem() {
        // Arrange
        String mensagem = "Senha inválida";

        // Act
        SenhaInvalidaException ex = new SenhaInvalidaException(mensagem);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_ComMensagemECausa_DeveCriarExcecaoComMensagemECausa() {
        // Arrange
        String mensagem = "Senha inválida";
        Throwable causa = new RuntimeException("Causa");

        // Act
        SenhaInvalidaException ex = new SenhaInvalidaException(mensagem, causa);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertEquals(causa, ex.getCause());
    }
} 