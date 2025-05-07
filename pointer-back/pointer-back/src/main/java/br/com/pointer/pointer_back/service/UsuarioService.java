package br.com.pointer.pointer_back.service;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.dto.EmailDTO;
import br.com.pointer.pointer_back.dto.UpdatePasswordDTO;
import br.com.pointer.pointer_back.exception.UsuarioNaoEncontradoException;
import br.com.pointer.pointer_back.mapper.UsuarioMapper;
import br.com.pointer.pointer_back.model.StatusUsuario;
import br.com.pointer.pointer_back.model.Usuario;
import br.com.pointer.pointer_back.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;

import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

@Service
public class UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UsuarioMapper usuarioMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;
    private final Keycloak keycloak;
    private final String realm;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            KeycloakAdminService keycloakAdminService,
            UsuarioMapper usuarioMapper,
            EmailService emailService,
            Keycloak keycloak,
            @Value("${keycloak.realm}") String realm) {
        this.usuarioRepository = usuarioRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.usuarioMapper = usuarioMapper;
        this.emailService = emailService;
        this.keycloak = keycloak;
        this.realm = realm;
    }

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioDTO dto) {
        try {
            String senhaPura = dto.getSenha();
            if (senhaPura == null) {
                senhaPura = gerarSenhaAleatoria();
                enviarSenhaPorEmail(dto.getEmail(), senhaPura, dto.getNome());
            }

            Usuario usuario = usuarioMapper.toEntity(dto);
            usuario.setSenha(passwordEncoder.encode(senhaPura));
                
            usuario = usuarioRepository.save(usuario);

            String userId = keycloakAdminService.createUserAndReturnId(
                    usuario.getNome(), usuario.getEmail(), senhaPura);

            keycloakAdminService.setUserPassword(userId, senhaPura);
            keycloakAdminService.assignRolesToUser(userId, obterRolesPorTipo(dto.getTipoUsuario()));

            return usuarioMapper.toResponseDTO(usuario);
        } catch (Exception e) {
            logger.error("Erro ao criar usuário: ", e);
            throw new RuntimeException("Erro ao criar usuário: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarUsuarios(PageRequest pageRequest, String setor, String perfil,
            String status) {
        Specification<Usuario> spec = Specification.where(null);
        
        spec = spec.and((root, query, cb) -> {
            assert query != null;
            query.orderBy(cb.desc(root.get("dataCriacao")));
            return null;
        });

        if (StringUtils.hasText(setor)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("setor"), setor));
        }

        if (StringUtils.hasText(perfil)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoUsuario"), perfil));
        }

        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return usuarioRepository.findAll(spec, pageRequest).map(usuarioMapper::toResponseDTO);
    }

    @Transactional
    public void alternarStatusUsuarioPorEmail(EmailDTO emailDTO) {
        Usuario usuario = usuarioRepository.findByEmail(emailDTO.getEmail())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(emailDTO.getEmail()));

        if (usuario.getStatus().equals(StatusUsuario.ATIVO)) {
            desativarUsuario(usuario);
        } else {
            ativarUsuario(usuario);
        }
    }

    private void desativarUsuario(Usuario usuario) {
        usuario.setStatus(StatusUsuario.INATIVO);
        keycloakAdminService.disableUser(usuario.getEmail());
    }

    private void ativarUsuario(Usuario usuario) {
        usuario.setStatus(StatusUsuario.ATIVO);
        keycloakAdminService.enableUser(usuario.getEmail());
    }

    @Transactional
    public String gerarSenhaAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder senha = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(caracteres.length());
            senha.append(caracteres.charAt(index));
        }
        return senha.toString();
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuarioComSincronizacaoKeycloak(UsuarioDTO dto, String id) {
        Usuario usuario = usuarioRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        try {
            UserRepresentation userRepresentation = criarUserRepresentation(dto);
            atualizarUsuarioNoKeycloak(userRepresentation);
        } catch (Exception e) {
            logger.error("Erro ao atualizar usuário no Keycloak: ", e);
            throw new RuntimeException("Erro ao atualizar usuário no Keycloak: " + e.getMessage());
        }
        
        usuarioMapper.updateEntityFromDTO(dto, usuario);
        usuario = usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(usuario);
    }

    private UserRepresentation criarUserRepresentation(UsuarioDTO dto) {
        UserRepresentation user = new UserRepresentation();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getEmail());
        user.setEnabled(dto.getStatus() == StatusUsuario.ATIVO);
        
        // Configurar nome e sobrenome
        String[] nomeCompleto = dto.getNome().split(" ", 2);
        user.setFirstName(nomeCompleto[0]);
        user.setLastName(nomeCompleto.length > 1 ? nomeCompleto[1] : "");
        
        // Configurar atributos adicionais se necessário
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("setor", Collections.singletonList(dto.getSetor()));
        attributes.put("tipoUsuario", Collections.singletonList(dto.getTipoUsuario()));
        user.setAttributes(attributes);
        
        return user;
    }

    private void atualizarUsuarioNoKeycloak(UserRepresentation userRepresentation) {
        // Busca o usuário no Keycloak pelo email que está no DTO (email atual)
        List<UserRepresentation> users = keycloak.realm(realm).users().search(userRepresentation.getEmail());
        
        if (users.isEmpty()) {
            logger.error("Usuário não encontrado no Keycloak com email: {}", userRepresentation.getEmail());
            throw new RuntimeException("Usuário não encontrado no Keycloak");
        }

        String userId = users.get(0).getId();
        logger.info("Usuário encontrado no Keycloak com ID: {}", userId);
        
        // Atualizar informações básicas do usuário
        keycloakAdminService.updateUser(userId, userRepresentation);
        
        // Atualizar roles
        Set<String> rolesAtuais = obterRolesAtuaisDoUsuario(userId);
        if (!rolesAtuais.isEmpty()) {
            keycloakAdminService.removeRolesFromUser(userId, rolesAtuais);
        }
        
        Set<String> novasRoles = obterRolesPorTipo(userRepresentation.getAttributes().get("tipoUsuario").get(0));
        keycloakAdminService.assignRolesToUser(userId, novasRoles);
    }

    private Set<String> obterRolesAtuaisDoUsuario(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<RoleRepresentation> roles = realmResource.users().get(userId).roles().realmLevel().listAll();
            return roles.stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Erro ao obter roles atuais do usuário: ", e);
            return Set.of();
        }
    }

    private Set<String> obterRolesPorTipo(String tipoUsuario) {
        return switch (tipoUsuario) {
            case "ADMIN" -> Set.of("colaborador", "admin");
            case "GESTOR" -> Set.of("gestor", "colaborador");
            default -> Set.of("colaborador");
        };
    }

    public void resetarSenhaComEmailEKeycloak(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(email));

        String novaSenha = gerarSenhaAleatoria();
        enviarSenhaPorEmail(email, novaSenha, usuario.getNome());
        atualizarSenhaNoBanco(usuario, novaSenha);
        atualizarSenhaNoKeycloak(email, novaSenha);
    }

    private void enviarSenhaPorEmail(String email, String senha, String nome) {
        emailService.sendPasswordEmail(email, senha, nome);
    }

    public void atualizarSenhaUsuario(UpdatePasswordDTO updatePasswordDTO) {
        Usuario usuario = usuarioRepository.findByEmail(updatePasswordDTO.getEmail())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(updatePasswordDTO.getEmail()));
        atualizarSenhaNoBanco(usuario, updatePasswordDTO.getSenha());
        atualizarSenhaNoKeycloak(updatePasswordDTO.getEmail(), updatePasswordDTO.getSenha());
    }

    private void atualizarSenhaNoKeycloak(String email, String senha) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(email);
        if (users.isEmpty())
            return;

        String userId = users.get(0).getId();
        keycloakAdminService.updatePassword(userId, senha);
    }

    private void atualizarSenhaNoBanco(Usuario usuario, String senha) {
        usuario.setSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(usuario);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(email));
    }

    public UsuarioResponseDTO buscarUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(email));
        return usuarioMapper.toResponseDTO(usuario);
    }
}