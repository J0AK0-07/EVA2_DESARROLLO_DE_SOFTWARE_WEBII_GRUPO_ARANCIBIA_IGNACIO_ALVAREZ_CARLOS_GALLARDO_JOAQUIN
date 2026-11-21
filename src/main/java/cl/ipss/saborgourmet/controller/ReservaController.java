package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.ReservaRequestDTO;
import cl.ipss.saborgourmet.dto.ReservaResponseDTO;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.service.ReservaService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de Reservas en "Sabor Gourmet". Implementa las operaciones para
 * crear, listar, consultar por identificador, modificar y cancelar reservas en línea.
 *
 * <p>Devuelve objetos ResponseEntity con códigos de respuesta HTTP estrictos (200, 201, 204).
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * Endpoint para consultar y listar las reservas actuales. Permite filtrar opcionalmente por
     * fecha, cliente, mesa o estado.
     *
     * <p>Retorna HTTP 200 (OK) con la lista de reservas enriquecidas.
     */
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fecha,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long mesaId,
            @RequestParam(required = false) EstadoReserva estado) {

        List<ReservaResponseDTO> reservas = reservaService.listar(fecha, clienteId, mesaId, estado);
        return ResponseEntity.ok(reservas);
    }

    /**
     * Endpoint para consultar una reserva específica por su identificador único. Retorna HTTP 200
     * (OK) con la información detallada de la reserva.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        ReservaResponseDTO reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(reserva);
    }

    /**
     * Endpoint para crear una nueva reserva en línea. Valida cliente, mesa, capacidad y ausencia de
     * reservas superpuestas. Retorna HTTP 201 (Created) con la reserva creada.
     */
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crear(@Valid @RequestBody ReservaRequestDTO dto) {
        ReservaResponseDTO nueva = reservaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    /**
     * Endpoint para modificar los datos de una reserva existente. Retorna HTTP 200 (OK) con la
     * reserva actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> modificar(
            @PathVariable Long id, @Valid @RequestBody ReservaRequestDTO dto) {
        ReservaResponseDTO actualizada = reservaService.modificar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Endpoint oficial para la cancelación de reservas. Método: PATCH Ruta:
     * /api/reservas/{id}/cancelar
     *
     * <p>Cambia el estado de la reserva a 'CANCELADA' y libera inmediatamente la disponibilidad de
     * la mesa. Acepta un motivo opcional en el cuerpo o como parámetro de consulta. Retorna HTTP
     * 200 (OK) con la reserva cancelada.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String motivo) {

        String motivoFinal = motivo;
        if (body != null && body.containsKey("motivo")) {
            motivoFinal = body.get("motivo");
        }

        ReservaResponseDTO cancelada = reservaService.cancelar(id, motivoFinal);
        return ResponseEntity.ok(cancelada);
    }

    /**
     * Endpoint alternativo de cancelación vía PUT para clientes REST que no soporten PATCH. Ruta:
     * /api/reservas/{id}/cancelar
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarPut(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String motivo) {
        return cancelar(id, body, motivo);
    }

    /**
     * Endpoint para eliminación física administrativa de una reserva. Retorna HTTP 204 (No Content)
     * sin cuerpo.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
