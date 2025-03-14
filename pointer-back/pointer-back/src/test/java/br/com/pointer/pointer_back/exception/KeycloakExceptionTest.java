package br.com.pointer.pointer_back.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakExceptionTest {

    @Test
    void constructor_ComMensagem_DeveCriarExcecaoComMensagem() {
        // Arrange
        String mensagem = "Erro no Keycloak";

        // Act
        KeycloakException ex = new KeycloakException(mensagem);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_ComMensagemECausa_DeveCriarExcecaoComMensagemECausa() {
        // Arrange
        String mensagem = "Erro no Keycloak";
        Throwable causa = new RuntimeException("Causa");

        // Act
        KeycloakException ex = new KeycloakException(mensagem, causa);

        // Assert
        assertEquals(mensagem, ex.getMessage());
        assertEquals(causa, ex.getCause());
    }
} 