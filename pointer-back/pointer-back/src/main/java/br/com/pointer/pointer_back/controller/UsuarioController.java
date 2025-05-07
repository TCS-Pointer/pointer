package br.com.pointer.pointer_back.controller;

import br.com.pointer.pointer_back.dto.UsuarioDTO;
import br.com.pointer.pointer_back.dto.UsuarioResponseDTO;
import br.com.pointer.pointer_back.dto.EmailCode;
import br.com.pointer.pointer_back.dto.EmailDTO;
import br.com.pointer.pointer_back.dto.UpdatePasswordDTO;
import br.com.pointer.pointer_back.service.EmailService;
import br.com.pointer.pointer_back.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;

    public UsuarioController(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
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
        usuarioService.alternarStatusUsuarioPorEmail(emailDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/atualizar-usuario/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(@PathVariable String id, @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioResponseDTO usuarioAtualizado = usuarioService.atualizarUsuarioComSincronizacaoKeycloak(usuarioDTO, id);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @PutMapping("/atualizar-senha")
    @PreAuthorize("hasRole('colaborador') or hasRole('admin') or hasRole('gestor')")
    public ResponseEntity<Void> atualizarSenha(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        usuarioService.atualizarSenhaUsuario(updatePasswordDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/esqueceu-senha")
    public ResponseEntity<Void> esqueceuSenha(@RequestBody EmailDTO emailDTO) {
        boolean exists = usuarioService.existsByEmail(emailDTO.getEmail());
        if (exists) {
            String nome = usuarioService.findByEmail(emailDTO.getEmail()).getNome();
            emailService.sendVerificationCodeEmail(emailDTO.getEmail(), nome);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping("/verificar-codigo")
    public ResponseEntity<Void> verificarCodigo(@RequestBody EmailCode emailCode) {
        boolean isValid = emailService.verifyCode(emailCode.getEmail(), emailCode.getCodigo());
        if (isValid) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        usuarioService.atualizarSenhaUsuario(updatePasswordDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verificar-email/{email}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> verificarEmail(@PathVariable String email) {
        boolean exists = usuarioService.existsByEmail(email);
        if (!exists) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('colaborador') or hasRole('admin') or hasRole('gestor')")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuario(@PathVariable String email) {
        UsuarioResponseDTO usuario = usuarioService.buscarUsuario(email);
        return ResponseEntity.ok(usuario);
    }
}