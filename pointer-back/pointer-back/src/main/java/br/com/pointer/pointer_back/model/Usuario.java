package br.com.pointer.pointer_back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, length = 6, nullable = false)
    private String matricula;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(unique = true, length = 11, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String cargo;

    @Column(nullable = false)
    private String setor;

    @Column(nullable = false)
    private boolean status = true;
}
