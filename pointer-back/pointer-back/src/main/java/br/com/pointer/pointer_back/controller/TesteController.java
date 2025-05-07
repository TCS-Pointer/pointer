package br.com.pointer.pointer_back.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
public class TesteController {

    @GetMapping("/listar")
    @PreAuthorize("hasRole('admin')")
    public String listar() {
        return "Teste";
    }

    @GetMapping("/listar2")
    @PreAuthorize("hasRole('user')")
    public String listar2() {
        return "Teste2";
    }

    @GetMapping("/listar3")
    @PreAuthorize("hasRole('admin') or hasRole('user')")
    public String listar3() {
        return "Teste3";
    }
}
