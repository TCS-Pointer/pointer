package br.com.pointer.pointer_back.service;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.exception.EmailInvalidoException;
import br.com.pointer.pointer_back.exception.KeycloakException;
import br.com.pointer.pointer_back.exception.SenhaInvalidaException;
import br.com.pointer.pointer_back.exception.UsuarioJaExisteException;
import br.com.pointer.pointer_back.mapper.UsuarioMapper;
import br.com.pointer.pointer_back.model.Usuario;
import br.com.pointer.pointer_back.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final KeycloakAdminService keycloakAdminService;

    public UsuarioService(UsuarioRepository usuarioRepository, KeycloakAdminService keycloakAdminService) {
        this.usuarioRepository = usuarioRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioDTO usuarioDTO) {
        try {
            Usuario usuario = UsuarioMapper.toEntity(usuarioDTO);
            usuario.setMatricula(gerarMatricula());

            usuario = usuarioRepository.save(usuario);

            // Criar usuário no Keycloak
            String userId = keycloakAdminService.createUserAndReturnId(usuario.getNome(), usuario.getEmail(), usuarioDTO.getSenha());

            // Definir senha do usuário
            keycloakAdminService.setUserPassword(userId, usuarioDTO.getSenha());

            if(usuario.getSetor().equals("Recursos Humanos")||usuario.getSetor().equals("Diretoria")||usuario.getCargo().equals("Administrador")){
                keycloakAdminService.assignRolesToUser(userId, Set.of("user", "admin"));
            }else{
                keycloakAdminService.assignRolesToUser(userId, Set.of("user"));
            }

            return UsuarioMapper.toResponseDTO(usuario);
        } catch (UsuarioJaExisteException | EmailInvalidoException | SenhaInvalidaException e) {
            throw e;
        } catch (Exception e) {
            throw new KeycloakException("Erro ao criar usuário: " + e.getMessage(), e);
        }
    }

    private String gerarMatricula() {
        long count = usuarioRepository.count() + 1;
        return String.format("%06d", count);
    }
}
