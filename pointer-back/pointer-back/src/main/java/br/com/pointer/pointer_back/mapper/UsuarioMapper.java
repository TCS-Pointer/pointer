package br.com.pointer.pointer_back.mapper;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UsuarioMapper {

    public static Usuario toEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(new BCryptPasswordEncoder().encode(dto.getSenha()));
        usuario.setCpf(dto.getCpf());
        usuario.setCargo(dto.getCargo());
        usuario.setSetor(dto.getSetor());
        return usuario;
    }

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getMatricula(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getCargo(),
                usuario.getSetor(),
                usuario.isStatus()
        );
    }
}
