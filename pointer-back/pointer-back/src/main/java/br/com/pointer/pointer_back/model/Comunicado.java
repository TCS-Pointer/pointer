package br.com.pointer.pointer_back.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "comunicado")
public class Comunicado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String setor;

    @Column(nullable = false)
    private String cargo;

    @Column(name = "dt_publicacao", nullable = false)
    private LocalDateTime dataPublicacao;

    @PrePersist
    protected void onCreate() {
        dataPublicacao = LocalDateTime.now();
    }
}