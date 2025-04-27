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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.stream.Collectors;

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
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarUsuarios(PageRequest pageRequest, String setor, String perfil,
            String status) {
        Specification<Usuario> spec = Specification.where(null);

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
    public UsuarioResponseDTO atualizarUsuarioComSincronizacaoKeycloak(UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(dto.getEmail()));

        usuarioMapper.updateEntityFromDTO(dto, usuario);
        usuario = usuarioRepository.save(usuario);
        atualizarUsuarioNoKeycloak(dto.getEmail(), dto);

        return usuarioMapper.toResponseDTO(usuario);
    }

    private void atualizarUsuarioNoKeycloak(String emailAtual, UsuarioDTO dto) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(emailAtual);
        if (users.isEmpty())
            return;

        UserRepresentation user = criarUserRepresentation(dto);
        String userId = users.get(0).getId();

        keycloakAdminService.updateUser(userId, user);

        Set<String> rolesAtuais = obterRolesAtuaisDoUsuario(userId);

        if (!rolesAtuais.isEmpty()) {
            keycloakAdminService.removeRolesFromUser(userId, rolesAtuais);
        }

        Set<String> novasRoles = obterRolesPorTipo(dto.getTipoUsuario());
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

    private UserRepresentation criarUserRepresentation(UsuarioDTO dto) {
        UserRepresentation user = new UserRepresentation();

        definirNomeCompleto(user, dto.getNome());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getEmail());
        if (dto.getStatus().equals(StatusUsuario.ATIVO)) {
            user.setEnabled(true);
        } else {
            user.setEnabled(false);
        }

        return user;
    }

    private void definirNomeCompleto(UserRepresentation user, String nomeCompleto) {
        String[] partesDoNome = nomeCompleto.trim().split("\\s+");
        user.setFirstName(partesDoNome[0]);
        user.setLastName(partesDoNome.length > 1
                ? String.join(" ", Arrays.copyOfRange(partesDoNome, 1, partesDoNome.length))
                : "");
    }

    private Set<String> obterRolesPorTipo(String tipoUsuario) {
        return switch (tipoUsuario) {
            case "ADMIN" -> Set.of("user", "admin");
            case "GESTOR" -> Set.of("gestor", "user");
            default -> Set.of("user");
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
}