package br.com.pointer.pointer_back.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Map;

public class JWTConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Collection<String>> realm_access = jwt.getClaim("realm_access");
        Collection<String> roles = realm_access.get("roles");
        var grants = roles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
        return new JwtAuthenticationToken(jwt, grants);
    }

}
