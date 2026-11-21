package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.*;
import cl.ipss.saborgourmet.exception.*;
import cl.ipss.saborgourmet.model.*;
import cl.ipss.saborgourmet.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

/** Administración de mesas y consulta de disponibilidad usando repositorios JPA. */
@Service
@Transactional(readOnly = true)
public class MesaService {
    private final MesaRepository mesas;
    private final ReservaRepository reservas;
    private final HorarioService horarios;

    public MesaService(MesaRepository mesas, ReservaRepository reservas, HorarioService horarios) {
        this.mesas = mesas;
        this.reservas = reservas;
        this.horarios = horarios;
    }

    public List<Mesa> listarTodas() {
        return mesas.findAllByOrderByNumeroAsc();
    }

    public Mesa obtenerPorId(Long id) {
        if (id == null) throw new BadRequestException("Indique la mesa.");
        return mesas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada."));
    }

    @Transactional
    public Mesa crear(MesaDTO dto) {
        validar(dto, null);
        return mesas.save(
                new Mesa(
                        null,
                        dto.getNumero(),
                        dto.getCapacidad(),
                        ubicacion(dto),
                        dto.getActiva() == null || dto.getActiva()));
    }

    @Transactional
    public Mesa actualizar(Long id, MesaDTO dto) {
        Mesa mesa =
                mesas.bloquearPorId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada."));
        validar(dto, id);
        if (reservas.existsByMesa_IdAndEstadoAndCantidadPersonasGreaterThan(
                id, EstadoReserva.ACTIVA, dto.getCapacidad()))
            throw new ConflictException(
                    "La capacidad propuesta deja sin cupo a una reserva activa. Modifique esa"
                        + " reserva primero.");
        if (Boolean.FALSE.equals(dto.getActiva())
                && reservas.buscar(null, null, null, id, EstadoReserva.ACTIVA).stream()
                        .anyMatch(r -> r.getFin().isAfter(LocalDateTime.now(HorarioService.ZONA))))
            throw new ConflictException(
                    "Reasigne o cancele las reservas futuras antes de desactivar esta mesa.");
        mesa.setNumero(dto.getNumero());
        mesa.setCapacidad(dto.getCapacidad());
        mesa.setUbicacion(ubicacion(dto));
        if (dto.getActiva() != null) mesa.setActiva(dto.getActiva());
        return mesas.save(mesa);
    }

    @Transactional
    public void eliminar(Long id) {
        Mesa mesa =
                mesas.bloquearPorId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada."));
        if (reservas.existsByMesa_Id(id))
            throw new ConflictException(
                    "La mesa tiene reservas asociadas. Conserve el historial y desactívela cuando"
                        + " no tenga reservas futuras.");
        mesas.delete(mesa);
    }

    public DisponibilidadConsultaDTO consultarDisponibilidad(
            LocalDate fecha, LocalTime hora, Integer duracion, Integer personas) {
        return consultarDisponibilidad(fecha, hora, duracion, personas, null);
    }

    /** Al editar se excluye la propia reserva del cálculo, manteniendo la validación al guardar. */
    public DisponibilidadConsultaDTO consultarDisponibilidad(
            LocalDate fecha, LocalTime hora, Integer duracion, Integer personas, Long excluir) {
        int minutos = horarios.validar(fecha, hora, duracion, personas);
        LocalDateTime inicio = fecha.atTime(hora);
        return new DisponibilidadConsultaDTO(
                fecha,
                hora,
                minutos,
                personas,
                mesas.buscarDisponibles(inicio, inicio.plusMinutes(minutos), personas, excluir));
    }

    private String ubicacion(MesaDTO dto) {
        return dto.getUbicacion() == null || dto.getUbicacion().isBlank()
                ? "Salón principal"
                : dto.getUbicacion().trim();
    }

    private void validar(MesaDTO dto, Long excluir) {
        if (dto == null || dto.getNumero() == null || dto.getNumero() < 1 || dto.getNumero() > 9999)
            throw new BadRequestException("El número de mesa debe estar entre 1 y 9999.");
        if (dto.getCapacidad() == null || dto.getCapacidad() < 1 || dto.getCapacidad() > 100)
            throw new BadRequestException("La capacidad debe estar entre 1 y 100 personas.");
        if (dto.getUbicacion() != null && dto.getUbicacion().trim().length() > 100)
            throw new BadRequestException("La ubicación admite hasta 100 caracteres.");
        mesas.findByNumero(dto.getNumero())
                .ifPresent(
                        m -> {
                            if (!m.getId().equals(excluir))
                                throw new ConflictException("Ya existe una mesa con ese número.");
                        });
    }
}
