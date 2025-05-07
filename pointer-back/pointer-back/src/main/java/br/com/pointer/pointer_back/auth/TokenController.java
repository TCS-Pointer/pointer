package br.com.pointer.pointer_back.auth;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/token")
@RestController
public class TokenController {

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String authServerUrl;

    @Value("${keycloak.realm:pointer}")
    private String realm;

    @Autowired
    private Keycloak keycloak;

    @PostMapping
    public ResponseEntity<String> token(@RequestBody User user) {
        RealmResource realmResource = keycloak.realm(realm);
        List<UserRepresentation> users = realmResource.users().search(user.username());

        if (!users.isEmpty()) {
            UserRepresentation userRep = users.get(0);
            UserResource userResource = realmResource.users().get(userRep.getId());
            userResource.logout();
        }

        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", user.clientId());
        formData.add("client_secret", clientSecret);
        formData.add("username", user.username());
        formData.add("password", user.password());
        formData.add("grant_type", user.grantType());
        formData.add("scope", "openid");
        formData.add("expires_in", "100000");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);
        return restTemplate.postForEntity(tokenUrl, entity, String.class);
    }

    public record User(String password, String clientId, String username, String grantType) {
    }
}
