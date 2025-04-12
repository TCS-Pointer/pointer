package br.com.pointer.pointer_back.service;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.dto.EmailDTO;
import br.com.pointer.pointer_back.exception.EmailInvalidoException;
import br.com.pointer.pointer_back.exception.KeycloakException;
import br.com.pointer.pointer_back.exception.SenhaInvalidaException;
import br.com.pointer.pointer_back.exception.UsuarioJaExisteException;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UsuarioMapper usuarioMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            KeycloakAdminService keycloakAdminService,
            UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioDTO usuarioDTO) {
        try {
            Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

            // Criptografa a senha antes de salvar no banco
            String senhaCriptografada = passwordEncoder.encode(usuarioDTO.getSenha());
            usuario.setSenha(senhaCriptografada);

            usuario = usuarioRepository.save(usuario);

            // Criar usuário no Keycloak
            String userId = keycloakAdminService.createUserAndReturnId(usuario.getNome(), usuario.getEmail(),
                    usuarioDTO.getSenha());

            // Definir senha do usuário
            keycloakAdminService.setUserPassword(userId, usuarioDTO.getSenha());

            // Atribuir roles baseado no tipo de usuário
            if (usuario.getTipoUsuario().equals("ADMIN")) {
                keycloakAdminService.assignRolesToUser(userId, Set.of("user", "admin"));
            } else {
                keycloakAdminService.assignRolesToUser(userId, Set.of("user"));
            }

            return usuarioMapper.toResponseDTO(usuario);
        } catch (UsuarioJaExisteException | EmailInvalidoException | SenhaInvalidaException e) {
            throw e;
        } catch (Exception e) {
            throw new KeycloakException("Erro ao criar usuário: " + e.getMessage(), e);
        }
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
    public void alterarStatus(EmailDTO emailDTO) {
        Usuario usuario = usuarioRepository.findByEmail(emailDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getStatus().equals(StatusUsuario.ATIVO)) {
            usuario.setStatus(StatusUsuario.INATIVO);
            keycloakAdminService.disableUser(usuario.getEmail());
        } else {
            usuario.setStatus(StatusUsuario.ATIVO);
            keycloakAdminService.enableUser(usuario.getEmail());
        }

        usuarioRepository.save(usuario);
    }
}