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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioDTO usuarioDTO;
    private Usuario usuario;
    private static final String USER_ID = "123";

    @BeforeEach
    void setUp() {
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setNome("Teste");
        usuarioDTO.setEmail("teste@email.com");
        usuarioDTO.setSenha("senha123");
        usuarioDTO.setSetor("TI");
        usuarioDTO.setCargo("Desenvolvedor");

        usuario = UsuarioMapper.toEntity(usuarioDTO);
        usuario.setMatricula("000001");
    }

    @Test
    void criarUsuario_DeveCriarUsuarioComSucesso() {
        // Arrange
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(keycloakAdminService.createUserAndReturnId(anyString(), anyString(), anyString()))
            .thenReturn(USER_ID);

        // Act
        UsuarioResponseDTO result = usuarioService.criarUsuario(usuarioDTO);

        // Assert
        assertNotNull(result);
        assertEquals(usuario.getMatricula(), result.getMatricula());
        assertEquals(usuario.getNome(), result.getNome());
        assertEquals(usuario.getEmail(), result.getEmail());
        verify(usuarioRepository).save(any(Usuario.class));
        verify(keycloakAdminService).createUserAndReturnId(anyString(), anyString(), anyString());
        verify(keycloakAdminService).setUserPassword(USER_ID, usuarioDTO.getSenha());
        verify(keycloakAdminService).assignRolesToUser(USER_ID, Set.of("user"));
    }

    @Test
    void criarUsuario_DeveAtribuirRoleAdminParaSetoresEspecificos() {
        // Arrange
        usuarioDTO.setSetor("Recursos Humanos");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(keycloakAdminService.createUserAndReturnId(anyString(), anyString(), anyString()))
            .thenReturn(USER_ID);

        // Act
        usuarioService.criarUsuario(usuarioDTO);

        // Assert
        verify(keycloakAdminService).assignRolesToUser(USER_ID, Set.of("user", "admin"));
    }

    @Test
    void criarUsuario_DevePropagarExcecaoQuandoUsuarioJaExiste() {
        // Arrange
        when(keycloakAdminService.createUserAndReturnId(anyString(), anyString(), anyString()))
            .thenThrow(new UsuarioJaExisteException("Usuário já existe"));

        // Act & Assert
        UsuarioJaExisteException exception = assertThrows(UsuarioJaExisteException.class, () -> 
            usuarioService.criarUsuario(usuarioDTO)
        );
        assertEquals("Usuário já existe", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void criarUsuario_DevePropagarExcecaoQuandoEmailInvalido() {
        // Arrange
        when(keycloakAdminService.createUserAndReturnId(anyString(), anyString(), anyString()))
            .thenThrow(new EmailInvalidoException("Email inválido"));

        // Act & Assert
        EmailInvalidoException exception = assertThrows(EmailInvalidoException.class, () -> 
            usuarioService.criarUsuario(usuarioDTO)
        );
        assertEquals("Email inválido", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void criarUsuario_DevePropagarExcecaoQuandoSenhaInvalida() {
        // Arrange
        when(keycloakAdminService.createUserAndReturnId(anyString(), anyString(), anyString()))
            .thenThrow(new SenhaInvalidaException("Senha inválida"));

        // Act & Assert
        SenhaInvalidaException exception = assertThrows(SenhaInvalidaException.class, () -> 
            usuarioService.criarUsuario(usuarioDTO)
        );
        assertEquals("Senha inválida", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void criarUsuario_DevePropagarExcecaoQuandoErroNoKeycloak() {
        // Arrange
        when(keycloakAdminService.createUserAndReturnId(anyString(), anyString(), anyString()))
            .thenThrow(new KeycloakException("Erro no Keycloak"));

        // Act & Assert
        KeycloakException exception = assertThrows(KeycloakException.class, () -> 
            usuarioService.criarUsuario(usuarioDTO)
        );
        assertEquals("Erro no Keycloak", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
} 