package br.com.pointer.pointer_back.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioJaExisteExceptionTest {

    @Test
    void constructor_ComMensagem_DeveCriarExcecaoComMensagem() {
        // Arrange
        String mensagem = "Usuário já existe";

        // Act
        UsuarioJaExisteException ex = new UsuarioJaExisteException(mensagem);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_ComMensagemECausa_DeveCriarExcecaoComMensagemECausa() {
        // Arrange
        String mensagem = "Usuário já existe";
        Throwable causa = new RuntimeException("Causa");

        // Act
        UsuarioJaExisteException ex = new UsuarioJaExisteException(mensagem, causa);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertEquals(causa, ex.getCause());
    }
} 