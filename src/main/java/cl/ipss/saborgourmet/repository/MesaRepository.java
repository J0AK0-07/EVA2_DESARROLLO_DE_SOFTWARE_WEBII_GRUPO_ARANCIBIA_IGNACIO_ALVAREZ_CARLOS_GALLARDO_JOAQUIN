package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Mesa;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

/** Repositorio JPA de mesas; calcula disponibilidad en una sola consulta con NOT EXISTS. */
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    Optional<Mesa> findByNumero(Integer numero);

    List<Mesa> findAllByOrderByNumeroAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mesa m where m.id = :id")
    Optional<Mesa> bloquearPorId(@Param("id") Long id);

    @Query(
            """
            select m from Mesa m where m.activa = true and m.capacidad >= :personas
            and not exists (select r.id from Reserva r where r.mesa = m
              and r.estado = cl.ipss.saborgourmet.model.EstadoReserva.ACTIVA
              and r.inicio < :fin and r.fin > :inicio
              and (:excluir is null or r.id <> :excluir))
            order by m.capacidad, m.numero
            """)
    List<Mesa> buscarDisponibles(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("personas") int personas,
            @Param("excluir") Long excluir);
}
