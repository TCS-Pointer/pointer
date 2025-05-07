package br.com.pointer.pointer_back.service;

import br.com.pointer.pointer_back.exception.EmailInvalidoException;
import br.com.pointer.pointer_back.exception.KeycloakException;
import br.com.pointer.pointer_back.exception.SenhaInvalidaException;
import br.com.pointer.pointer_back.exception.UsuarioJaExisteException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminService {

    private Keycloak keycloak;
    private final String realm;

    public KeycloakAdminService(
            Keycloak keycloak,
            @Value("${keycloak.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    // Método para testes
    void setKeycloak(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Cria um usuário no Keycloak e retorna o ID
     * 
     * @throws KeycloakException se houver erro na criação do usuário
     */
    public String createUserAndReturnId(String nome, String email, String senha) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new EmailInvalidoException("Email inválido");
        }
        if (senha == null || senha.length() < 8) {
            throw new SenhaInvalidaException("Senha deve ter pelo menos 8 caracteres");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Verifica se já existe usuário com este email
            if (!usersResource.search(email).isEmpty()) {
                throw new UsuarioJaExisteException("Já existe um usuário com este email");
            }

            String[] nomeRealArray = nome.split(" ");
            String nomeReal = nomeRealArray[0];
            String sobrenomeReal = nomeRealArray[1];

            UserRepresentation user = new UserRepresentation();
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(nomeReal);
            user.setLastName(sobrenomeReal);
            user.setEnabled(true);
            user.setEmailVerified(false);

            try (Response response = usersResource.create(user)) {
                if (response.getStatus() == 201) {
                    String location = response.getHeaderString("Location");
                    String userId = location.substring(location.lastIndexOf('/') + 1);

                    // Define a senha do usuário
                    setUserPassword(userId, senha);

                    return userId;
                } else {
                    String error = response.readEntity(String.class);
                    throw new KeycloakException(
                            "Erro ao criar usuário no Keycloak. Status: " + response.getStatus() + ". Erro: " + error);
                }
            }
        } catch (Exception e) {
            if (e instanceof KeycloakException || e instanceof UsuarioJaExisteException ||
                    e instanceof EmailInvalidoException || e instanceof SenhaInvalidaException) {
                throw e;
            }
            throw new KeycloakException("Erro ao criar usuário: " + e.getMessage(), e);
        }
    }

    /**
     * Define a senha do usuário
     * 
     * @throws KeycloakException se houver erro ao definir a senha
     */
    public void setUserPassword(String userId, String password) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do usuário não pode ser vazio");
        }
        if (password == null || password.length() < 8) {
            throw new SenhaInvalidaException("Senha deve ter pelo menos 8 caracteres");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            realmResource.users().get(userId).resetPassword(credential);
        } catch (Exception e) {
            if (e instanceof SenhaInvalidaException) {
                throw e;
            }
            throw new KeycloakException("Erro ao definir senha do usuário: " + e.getMessage(), e);
        }
    }

    /**
     * Atribui roles ao usuário
     * 
     * @throws KeycloakException se houver erro ao atribuir as roles
     */
    public void assignRolesToUser(String userId, Set<String> roles) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do usuário não pode ser vazio");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Ao menos uma role deve ser especificada");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<RoleRepresentation> roleRepresentations = roles.stream()
                    .map(roleName -> {
                        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                        if (role == null) {
                            throw new IllegalArgumentException("Role não encontrada: " + roleName);
                        }
                        return role;
                    })
                    .collect(Collectors.toList());

            realmResource.users().get(userId).roles().realmLevel().add(roleRepresentations);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new KeycloakException("Erro ao atribuir roles ao usuário: " + e.getMessage(), e);
        }
    }

    public void disableUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserRepresentation user = realmResource.users().search(email).stream()
                    .findFirst()
                    .orElseThrow(() -> new KeycloakException("Usuário não encontrado: " + email));

            user.setEnabled(false);
            
            realmResource.users().get(user.getId()).update(user);
            realmResource.users().get(user.getId()).logout();
        } catch (Exception e) {
            throw new KeycloakException("Erro ao desativar usuário: " + e.getMessage(), e);
        }
    }

    public void enableUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserRepresentation user = realmResource.users().search(email).stream()
                    .findFirst()
                    .orElseThrow(() -> new KeycloakException("Usuário não encontrado: " + email));

            user.setEnabled(true);
            realmResource.users().get(user.getId()).update(user);
        } catch (Exception e) {
            throw new KeycloakException("Erro ao ativar usuário: " + e.getMessage(), e);
        }
    }

    public void updateUser(String userId, UserRepresentation user) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do usuário não pode ser vazio");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.users().get(userId).update(user);
        } catch (Exception e) {
            throw new KeycloakException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }

    }

    public void removeRolesFromUser(String userId, Set<String> roles) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do usuário não pode ser vazio");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Ao menos uma role deve ser especificada");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<RoleRepresentation> roleRepresentations = roles.stream()
                    .map(roleName -> {
                        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                        if (role == null) {
                            throw new IllegalArgumentException("Role não encontrada: " + roleName);
                        }
                        return role;
                    })
                    .collect(Collectors.toList());

            realmResource.users().get(userId).roles().realmLevel().remove(roleRepresentations);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new KeycloakException("Erro ao remover roles do usuário: " + e.getMessage(), e);
        }
    }

    public void updatePassword(String userId, String password) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do usuário não pode ser vazio");
        }
        if (password == null || password.length() < 8) {
            throw new SenhaInvalidaException("Senha deve ter pelo menos 8 caracteres");
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            
            // Verifica se o usuário existe
            UserRepresentation user = userResource.toRepresentation();
            if (user == null) {
                throw new KeycloakException("Usuário não encontrado com o ID: " + userId);
            }

            // Cria o objeto de credenciais com a nova senha
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            // Tenta resetar a senha e captura a resposta
            try {
                userResource.resetPassword(credential);
            } catch (jakarta.ws.rs.WebApplicationException wae) {
                String errorMessage = wae.getResponse().readEntity(String.class);
                throw new KeycloakException("Erro ao resetar senha: Status " + wae.getResponse().getStatus() + " - " + errorMessage);
            }

        } catch (Exception e) {
            if (e instanceof KeycloakException) {
                throw e;
            }
            throw new KeycloakException("Erro ao atualizar senha: " + e.getMessage(), e);
        }
    }

}