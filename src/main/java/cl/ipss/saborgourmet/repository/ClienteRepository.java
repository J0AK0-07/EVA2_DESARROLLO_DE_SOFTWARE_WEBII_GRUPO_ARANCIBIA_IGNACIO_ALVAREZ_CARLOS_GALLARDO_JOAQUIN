package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Cliente;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * CRUD generado por Spring Data JPA. La consulta con bloqueo protege las relaciones concurrentes.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> bloquearPorId(@Param("id") Long id);
}
