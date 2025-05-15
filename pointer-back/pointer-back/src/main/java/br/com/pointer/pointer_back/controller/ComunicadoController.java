package br.com.pointer.pointer_back.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pointer.pointer_back.dto.ComunicadoDTO;
import br.com.pointer.pointer_back.service.ComunicadoService;

@RestController
@RequestMapping("/api/comunicados")
public class ComunicadoController {

    @Autowired
    private ComunicadoService comunicadoService;

    @GetMapping
    public ResponseEntity<List<ComunicadoDTO>> listarTodos() {
        return ResponseEntity.ok(comunicadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComunicadoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comunicadoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ComunicadoDTO> criar(@RequestBody ComunicadoDTO comunicadoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comunicadoService.criar(comunicadoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComunicadoDTO> atualizar(@PathVariable Long id, @RequestBody ComunicadoDTO comunicadoDTO) {
        return ResponseEntity.ok(comunicadoService.atualizar(id, comunicadoDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        comunicadoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
