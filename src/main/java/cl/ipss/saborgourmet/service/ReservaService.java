package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.*;
import cl.ipss.saborgourmet.exception.*;
import cl.ipss.saborgourmet.model.*;
import cl.ipss.saborgourmet.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

/**
 * Lógica de reservas compartida por las vistas MVC y la API REST. Cada escritura es una
 * transacción. El bloqueo de mesa serializa la comprobación y el guardado, por lo que dos
 * solicitudes simultáneas no pueden ocupar el mismo intervalo.
 */
@Service
@Transactional(readOnly = true)
public class ReservaService {
    private final ReservaRepository reservas;
    private final ClienteRepository clientes;
    private final MesaRepository mesas;
    private final HorarioService horarios;

    public ReservaService(
            ReservaRepository reservas,
            ClienteRepository clientes,
            MesaRepository mesas,
            HorarioService horarios) {
        this.reservas = reservas;
        this.clientes = clientes;
        this.mesas = mesas;
        this.horarios = horarios;
    }

    public List<ReservaResponseDTO> listar(
            LocalDate fecha, Long clienteId, Long mesaId, EstadoReserva estado) {
        return reservas
                .buscar(
                        fecha == null ? null : fecha.atStartOfDay(),
                        fecha == null ? null : fecha.plusDays(1).atStartOfDay(),
                        clienteId,
                        mesaId,
                        estado)
                .stream()
                .map(this::respuesta)
                .toList();
    }

    public ReservaResponseDTO obtenerPorId(Long id) {
        if (id == null) throw new BadRequestException("Indique la reserva.");
        return respuesta(
                reservas.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Reserva no encontrada.")));
    }

    @Transactional
    public ReservaResponseDTO crear(ReservaRequestDTO dto) {
        Reserva reserva = new Reserva();
        aplicarDatos(reserva, dto);
        return respuesta(reservas.saveAndFlush(reserva));
    }

    @Transactional
    public ReservaResponseDTO modificar(Long id, ReservaRequestDTO dto) {
        Reserva reserva = bloqueada(id);
        if (reserva.getEstado() != EstadoReserva.ACTIVA)
            throw new BadRequestException("Solo se pueden modificar reservas activas.");
        aplicarDatos(reserva, dto);
        return respuesta(reservas.saveAndFlush(reserva));
    }

    @Transactional
    public ReservaResponseDTO cancelar(Long id, String motivo) {
        Reserva reserva = bloqueada(id);
        if (reserva.getEstado() != EstadoReserva.ACTIVA)
            throw new ConflictException("La reserva ya no está activa.");
        reserva.setEstado(EstadoReserva.CANCELADA);
        if (motivo != null && !motivo.isBlank()) {
            if (motivo.length() > 200)
                throw new BadRequestException("El motivo admite hasta 200 caracteres.");
            String actual = reserva.getObservaciones() == null ? "" : reserva.getObservaciones();
            String nota = actual + " [Cancelación: " + motivo.trim() + "]";
            if (nota.length() > 1000)
                throw new BadRequestException(
                        "Las observaciones y el motivo superan 1000 caracteres.");
            reserva.setObservaciones(nota);
        }
        return respuesta(reservas.saveAndFlush(reserva));
    }

    @Transactional
    public void eliminar(Long id) {
        reservas.delete(bloqueada(id));
    }

    private Reserva bloqueada(Long id) {
        if (id == null) throw new BadRequestException("Indique la reserva.");
        return reservas.bloquearPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));
    }

    private void aplicarDatos(Reserva reserva, ReservaRequestDTO dto) {
        if (dto == null || dto.getClienteId() == null || dto.getMesaId() == null)
            throw new BadRequestException("Seleccione un cliente y una mesa.");
        int minutos =
                horarios.validar(
                        dto.getFecha(),
                        dto.getHora(),
                        dto.getDuracionMinutos(),
                        dto.getCantidadPersonas());
        if (dto.getObservaciones() != null && dto.getObservaciones().length() > 1000)
            throw new BadRequestException("Las observaciones admiten hasta 1000 caracteres.");
        Cliente cliente =
                clientes.bloquearPorId(dto.getClienteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));
        // Mantener este bloqueo hasta el commit hace atómica la regla de no solapamiento.
        Mesa mesa =
                mesas.bloquearPorId(dto.getMesaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada."));
        if (!Boolean.TRUE.equals(mesa.getActiva()))
            throw new BadRequestException("La mesa está inactiva.");
        if (mesa.getCapacidad() < dto.getCantidadPersonas())
            throw new BadRequestException("La mesa no tiene capacidad suficiente.");
        LocalDateTime inicio = dto.getFecha().atTime(dto.getHora());
        if (reservas.existeSolapamiento(
                mesa.getId(), inicio, inicio.plusMinutes(minutos), reserva.getId()))
            throw new ConflictException(
                    "La mesa ya está reservada en ese horario. Consulte otra mesa o cambie el"
                        + " turno.");
        reserva.setCliente(cliente);
        reserva.setMesa(mesa);
        reserva.definirHorario(dto.getFecha(), dto.getHora(), minutos);
        reserva.setCantidadPersonas(dto.getCantidadPersonas());
        reserva.setObservaciones(
                dto.getObservaciones() == null ? "" : dto.getObservaciones().trim());
    }

    private ReservaResponseDTO respuesta(Reserva r) {
        ReservaResponseDTO dto = new ReservaResponseDTO();
        dto.setId(r.getId());
        dto.setClienteId(r.getClienteId());
        dto.setClienteNombre(r.getCliente().getNombre());
        dto.setClienteEmail(r.getCliente().getEmail());
        dto.setClienteTelefono(r.getCliente().getTelefono());
        dto.setMesaId(r.getMesaId());
        dto.setMesaNumero(r.getMesa().getNumero());
        dto.setMesaCapacidad(r.getMesa().getCapacidad());
        dto.setMesaUbicacion(r.getMesa().getUbicacion());
        dto.setFecha(r.getFecha());
        dto.setHora(r.getHora());
        dto.setHoraFin(r.getHoraFin());
        dto.setDuracionMinutos(r.getDuracionMinutos());
        dto.setCantidadPersonas(r.getCantidadPersonas());
        dto.setEstado(r.getEstado());
        dto.setObservaciones(r.getObservaciones());
        return dto;
    }
}
