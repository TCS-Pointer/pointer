package br.com.pointer.pointer_back.repository;

import br.com.pointer.pointer_back.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(:setor IS NULL OR u.setor = :setor) AND " +
           "(:cargo IS NULL OR u.cargo = :cargo) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<Usuario> findByFilters(
        @Param("setor") String setor,
        @Param("cargo") String cargo,
        @Param("status") String status,
        Pageable pageable
    );
}
