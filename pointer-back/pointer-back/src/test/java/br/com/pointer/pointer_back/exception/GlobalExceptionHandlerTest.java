package br.com.pointer.pointer_back.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUsuarioJaExisteException_DeveRetornarResponseEntityComStatus409() {
        // Arrange
        UsuarioJaExisteException ex = new UsuarioJaExisteException("Usuário já existe");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleUsuarioJaExisteException(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Usuário já existe", response.getBody().get("message"));
        assertEquals("Conflito", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleEmailInvalidoException_DeveRetornarResponseEntityComStatus400() {
        // Arrange
        EmailInvalidoException ex = new EmailInvalidoException("Email inválido");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleEmailInvalidoException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Email inválido", response.getBody().get("message"));
        assertEquals("Email Inválido", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleSenhaInvalidaException_DeveRetornarResponseEntityComStatus400() {
        // Arrange
        SenhaInvalidaException ex = new SenhaInvalidaException("Senha inválida");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleSenhaInvalidaException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Senha inválida", response.getBody().get("message"));
        assertEquals("Senha Inválida", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleKeycloakException_DeveRetornarResponseEntityComStatus500() {
        // Arrange
        KeycloakException ex = new KeycloakException("Erro no Keycloak");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleKeycloakException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Erro no Keycloak", response.getBody().get("message"));
        assertEquals("Erro no Keycloak", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleGlobalException_DeveRetornarResponseEntityComStatus500() {
        // Arrange
        Exception ex = new RuntimeException("Erro interno");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Erro interno do servidor", response.getBody().get("message"));
        assertEquals("Erro Interno", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }
} 