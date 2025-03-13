package br.com.pointer.pointer_back.controller;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')") 
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        UsuarioResponseDTO novoUsuario = usuarioService.criarUsuario(usuarioDTO);
        return ResponseEntity.ok(novoUsuario);
    }
}
