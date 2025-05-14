package br.com.pointer.pointer_back.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.PrePersist;

@Data
@Entity
@Table(name = "leitura_comunicado")
public class LeituraDeComunicado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "comunicado_id", nullable = false)
    private Comunicado comunicado;

    @Column(name = "dt_leitura", nullable = false)
    private LocalDateTime dataLeitura;

    @PrePersist
    protected void onCreate() {
        dataLeitura = LocalDateTime.now();
    }
}
