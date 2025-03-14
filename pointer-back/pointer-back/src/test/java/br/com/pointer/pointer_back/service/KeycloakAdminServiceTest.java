package br.com.pointer.pointer_back.service;

import br.com.pointer.pointer_back.exception.EmailInvalidoException;
import br.com.pointer.pointer_back.exception.KeycloakException;
import br.com.pointer.pointer_back.exception.SenhaInvalidaException;
import br.com.pointer.pointer_back.exception.UsuarioJaExisteException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private Response response;

    private KeycloakAdminService keycloakAdminService;

    private static final String REALM = "pointer";
    private static final String USER_ID = "123";
    private static final String EMAIL = "teste@email.com";
    private static final String NOME = "Teste";
    private static final String SENHA = "senha123";
    private static final String SERVER_URL = "http://localhost:8080";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String CLIENT_ID = "pointer";
    private static final String CLIENT_SECRET = "secret";

    @BeforeEach
    void setUp() {
        keycloakAdminService = new KeycloakAdminService(
            SERVER_URL,
            REALM,
            ADMIN_USERNAME,
            ADMIN_PASSWORD,
            CLIENT_ID,
            CLIENT_SECRET
        );
        keycloakAdminService.setKeycloak(keycloak);
    }

    @Test
    void createUserAndReturnId_DeveCriarUsuarioComSucesso() {
        // Arrange
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(EMAIL)).thenReturn(Collections.emptyList());
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location")).thenReturn("/users/" + USER_ID);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(usersResource.get(USER_ID)).thenReturn(userResource);

        // Act
        String result = keycloakAdminService.createUserAndReturnId(NOME, EMAIL, SENHA);

        // Assert
        assertEquals(USER_ID, result);
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userResource).resetPassword(any());
    }

    @Test
    void createUserAndReturnId_DeveLancarExcecaoQuandoEmailInvalido() {
        // Arrange
        String emailInvalido = "email.invalido";

        // Act & Assert
        assertThrows(EmailInvalidoException.class, () -> 
            keycloakAdminService.createUserAndReturnId(NOME, emailInvalido, SENHA)
        );
    }

    @Test
    void createUserAndReturnId_DeveLancarExcecaoQuandoSenhaInvalida() {
        // Arrange
        String senhaInvalida = "123";

        // Act & Assert
        assertThrows(SenhaInvalidaException.class, () -> 
            keycloakAdminService.createUserAndReturnId(NOME, EMAIL, senhaInvalida)
        );
    }

    @Test
    void createUserAndReturnId_DeveLancarExcecaoQuandoUsuarioJaExiste() {
        // Arrange
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        UserRepresentation usuarioExistente = new UserRepresentation();
        when(usersResource.search(EMAIL)).thenReturn(Arrays.asList(usuarioExistente));

        // Act & Assert
        assertThrows(UsuarioJaExisteException.class, () -> 
            keycloakAdminService.createUserAndReturnId(NOME, EMAIL, SENHA)
        );
    }

    @Test
    void setUserPassword_DeveDefinirSenhaComSucesso() {
        // Arrange
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(USER_ID)).thenReturn(userResource);

        // Act
        keycloakAdminService.setUserPassword(USER_ID, SENHA);

        // Assert
        verify(userResource).resetPassword(any());
    }

    @Test
    void setUserPassword_DeveLancarExcecaoQuandoSenhaInvalida() {
        // Arrange
        String senhaInvalida = "123";

        // Act & Assert
        assertThrows(SenhaInvalidaException.class, () -> 
            keycloakAdminService.setUserPassword(USER_ID, senhaInvalida)
        );
    }

    @Test
    void assignRolesToUser_DeveAtribuirRolesComSucesso() {
        // Arrange
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(anyString())).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);

        // Act
        keycloakAdminService.assignRolesToUser(USER_ID, Set.of("user", "admin"));

        // Assert
        verify(roleMappingResource).realmLevel().add(anyList());
    }

    @Test
    void assignRolesToUser_DeveLancarExcecaoQuandoRoleNaoExiste() {
        // Arrange
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            keycloakAdminService.assignRolesToUser(USER_ID, Set.of("role_inexistente"))
        );
    }

    @Test
    void assignRolesToUser_DeveLancarExcecaoQuandoRolesVazio() {
        // Arrange
        Set<String> roles = Collections.emptySet();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            keycloakAdminService.assignRolesToUser(USER_ID, roles)
        );
    }
} 