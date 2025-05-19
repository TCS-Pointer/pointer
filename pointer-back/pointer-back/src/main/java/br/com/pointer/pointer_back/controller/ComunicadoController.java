package br.com.pointer.pointer_back.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.com.pointer.pointer_back.common.ApiResponse;
import br.com.pointer.pointer_back.dto.ComunicadoDTO;
import br.com.pointer.pointer_back.service.ComunicadoService;

@RestController
@RequestMapping("/comunicados")
public class ComunicadoController {

    @Autowired
    private ComunicadoService comunicadoService;

    @GetMapping
    @PreAuthorize("hasRole('colaborador') or hasRole('admin') or hasRole('gestor')")
    public ResponseEntity<ApiResponse<List<ComunicadoDTO>>> listarTodos() {
        List<ComunicadoDTO> comunicados = comunicadoService.listarTodos();
        return ResponseEntity.ok(new ApiResponse<List<ComunicadoDTO>>().ok(comunicados, "Comunicados listados com sucesso"));
    }

    @GetMapping("/buscar-comunicado/{id}")
    @PreAuthorize("hasRole('colaborador') or hasRole('admin') or hasRole('gestor')")
    public ResponseEntity<ApiResponse<ComunicadoDTO>> buscarPorId(@PathVariable Long id) {
        ComunicadoDTO comunicado = comunicadoService.buscarPorId(id);
        return ResponseEntity.ok(new ApiResponse<ComunicadoDTO>().ok(comunicado, "Comunicado encontrado com sucesso"));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ComunicadoDTO>> criar(@RequestBody ComunicadoDTO comunicadoDTO) {
        ComunicadoDTO novoComunicado = comunicadoService.criar(comunicadoDTO);
        return ResponseEntity.ok(new ApiResponse<ComunicadoDTO>().ok(novoComunicado, "Comunicado criado com sucesso"));
    }

    @PutMapping("/atualizar-comunicado/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ComunicadoDTO>> atualizar(@PathVariable Long id, @RequestBody ComunicadoDTO comunicadoDTO) {
        ComunicadoDTO comunicadoAtualizado = comunicadoService.atualizar(id, comunicadoDTO);
        return ResponseEntity.ok(new ApiResponse<ComunicadoDTO>().ok(comunicadoAtualizado, "Comunicado atualizado com sucesso"));
    }

    @DeleteMapping("/deletar-comunicado/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        comunicadoService.deletar(id);
        return ResponseEntity.ok(new ApiResponse<Void>().ok(null, "Comunicado deletado com sucesso"));
    }
}
