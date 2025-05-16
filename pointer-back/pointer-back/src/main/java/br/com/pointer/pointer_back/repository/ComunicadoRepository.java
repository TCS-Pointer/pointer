package br.com.pointer.pointer_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.pointer.pointer_back.model.Comunicado;

@Repository
public interface ComunicadoRepository extends JpaRepository<Comunicado, Long> {
}
