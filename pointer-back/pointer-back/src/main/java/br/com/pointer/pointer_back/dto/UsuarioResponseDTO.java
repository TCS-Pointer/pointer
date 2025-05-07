package br.com.pointer.pointer_back.dto;

import br.com.pointer.pointer_back.model.StatusUsuario;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private StatusUsuario status;
    private String cargo;
    private String setor;
    private String tipoUsuario;
    private LocalDateTime dataCriacao;
}
