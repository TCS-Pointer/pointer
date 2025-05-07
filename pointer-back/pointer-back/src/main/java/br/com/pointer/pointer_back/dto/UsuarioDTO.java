package br.com.pointer.pointer_back.dto;

import br.com.pointer.pointer_back.model.StatusUsuario;
import lombok.Data;

@Data
public class UsuarioDTO {
    private String nome;
    private String email;
    private String senha;
    private StatusUsuario status;
    private String cargo;
    private String setor;
    private String tipoUsuario;
} 