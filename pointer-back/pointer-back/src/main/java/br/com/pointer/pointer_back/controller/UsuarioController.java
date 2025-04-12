package br.com.pointer.pointer_back.controller;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.dto.EmailDTO;
import br.com.pointer.pointer_back.service.UsuarioService;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) String perfil,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UsuarioResponseDTO> usuarios = usuarioService.listarUsuarios(pageRequest, setor, perfil, status);
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/alterar-status")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> alterarStatus(@RequestBody EmailDTO emailDTO) {
        usuarioService.alterarStatus(emailDTO);
        return ResponseEntity.ok().build();
    }

}