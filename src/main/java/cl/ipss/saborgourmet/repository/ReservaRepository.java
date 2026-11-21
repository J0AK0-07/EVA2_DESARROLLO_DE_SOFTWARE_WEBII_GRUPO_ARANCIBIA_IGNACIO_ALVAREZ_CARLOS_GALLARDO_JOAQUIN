package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.*;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

/** Consultas JPA de reservas. EntityGraph carga cliente y mesa sin consultas por cada fila. */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @Override
    @EntityGraph(attributePaths = {"cliente", "mesa"})
    Optional<Reserva> findById(Long id);

    @EntityGraph(attributePaths = {"cliente", "mesa"})
    @Query(
            """
            select r from Reserva r where (:desde is null or r.inicio >= :desde)
              and (:hasta is null or r.inicio < :hasta)
              and (:clienteId is null or r.cliente.id = :clienteId)
              and (:mesaId is null or r.mesa.id = :mesaId)
              and (:estado is null or r.estado = :estado)
            order by r.inicio desc, r.id desc
            """)
    List<Reserva> buscar(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("clienteId") Long clienteId,
            @Param("mesaId") Long mesaId,
            @Param("estado") EstadoReserva estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> bloquearPorId(@Param("id") Long id);

    @Query(
            """
            select (count(r) > 0) from Reserva r where r.mesa.id = :mesaId
              and r.estado = cl.ipss.saborgourmet.model.EstadoReserva.ACTIVA
              and r.inicio < :fin and r.fin > :inicio
              and (:excluir is null or r.id <> :excluir)
            """)
    boolean existeSolapamiento(
            @Param("mesaId") Long mesaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("excluir") Long excluir);

    boolean existsByCliente_Id(Long clienteId);

    boolean existsByMesa_Id(Long mesaId);

    boolean existsByMesa_IdAndEstadoAndCantidadPersonasGreaterThan(
            Long mesaId, EstadoReserva estado, int capacidad);
}
