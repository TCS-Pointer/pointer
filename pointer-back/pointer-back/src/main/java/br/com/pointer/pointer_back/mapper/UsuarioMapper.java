package br.com.pointer.pointer_back.mapper;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.model.Usuario;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UsuarioMapper {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioMapper.class);

    public Usuario toEntity(UsuarioDTO dto) {
        try {
            if (dto == null) {
                logger.error("DTO é nulo");
                return null;
            }

            Usuario usuario = new Usuario();
            usuario.setNome(dto.getNome());
            usuario.setEmail(dto.getEmail());
            usuario.setSenha(dto.getSenha());
            usuario.setStatus(dto.getStatus());
            usuario.setCargo(dto.getCargo());
            usuario.setSetor(dto.getSetor());
            usuario.setTipoUsuario(dto.getTipoUsuario());
            return usuario;
        } catch (Exception e) {
            logger.error("Erro ao converter DTO para entidade: ", e);
            throw new RuntimeException("Erro ao converter DTO para entidade: " + e.getMessage());
        }
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        try {
            if (usuario == null) {
                logger.error("Usuário é nulo");
                return null;
            }

            UsuarioResponseDTO dto = new UsuarioResponseDTO();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());
            dto.setStatus(usuario.getStatus());
            dto.setCargo(usuario.getCargo());
            dto.setSetor(usuario.getSetor());
            dto.setTipoUsuario(usuario.getTipoUsuario());
            dto.setDataCriacao(usuario.getDataCriacao());
            return dto;
        } catch (Exception e) {
            logger.error("Erro ao converter entidade para DTO: ", e);
            throw new RuntimeException("Erro ao converter entidade para DTO: " + e.getMessage());
        }
    }

    public void updateEntityFromDTO(UsuarioDTO dto, Usuario usuario) {
        try {
            if (dto == null || usuario == null) {
                logger.error("DTO ou Usuário é nulo");
                return;
            }

            usuario.setNome(dto.getNome());
            usuario.setEmail(dto.getEmail());
            if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
                usuario.setSenha(dto.getSenha());
            }
            usuario.setStatus(dto.getStatus());
            usuario.setCargo(dto.getCargo());
            usuario.setSetor(dto.getSetor());
            usuario.setTipoUsuario(dto.getTipoUsuario());
        } catch (Exception e) {
            logger.error("Erro ao atualizar entidade com DTO: ", e);
            throw new RuntimeException("Erro ao atualizar entidade com DTO: " + e.getMessage());
        }
    }
}
