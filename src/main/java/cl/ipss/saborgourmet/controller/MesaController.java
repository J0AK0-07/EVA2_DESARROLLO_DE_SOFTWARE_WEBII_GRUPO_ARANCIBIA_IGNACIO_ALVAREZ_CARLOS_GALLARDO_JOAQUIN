package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.DisponibilidadConsultaDTO;
import cl.ipss.saborgourmet.dto.MesaDTO;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.service.MesaService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador REST para la gestión de Mesas y la consulta de Disponibilidad en "Sabor Gourmet".
 * Permite al personal actualizar la configuración física de las mesas y a los
 * clientes/recepcionistas consultar qué mesas se encuentran libres para un turno u horario
 * específico.
 */
@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    /**
     * Endpoint para listar todas las mesas configuradas en el restaurante. Retorna HTTP 200 (OK)
     * con el arreglo JSON de mesas.
     */
    @GetMapping
    public ResponseEntity<List<Mesa>> listarTodas() {
        List<Mesa> mesas = mesaService.listarTodas();
        return ResponseEntity.ok(mesas);
    }

    /**
     * Endpoint para consultar una mesa por su ID único. Retorna HTTP 200 (OK) con la información de
     * la mesa.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Mesa> obtenerPorId(@PathVariable Long id) {
        Mesa mesa = mesaService.obtenerPorId(id);
        return ResponseEntity.ok(mesa);
    }

    /**
     * Endpoint fundamental para consultar disponibilidad de mesas en línea. Criterios: fecha, hora
     * de inicio, duración estimada en minutos y cantidad de comensales.
     *
     * <p>Ejemplo de consulta: GET
     * /api/mesas/disponibles?fecha=2026-11-20&hora=13:00&personas=4&duracion=120
     *
     * <p>Retorna HTTP 200 (OK) con la información detallada del turno y las mesas libres.
     */
    @GetMapping("/disponibles")
    public ResponseEntity<DisponibilidadConsultaDTO> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(required = false, defaultValue = "120") Integer duracion,
            @RequestParam(name = "personas") Integer cantidadPersonas,
            @RequestParam(required = false) Long excluirReservaId) {

        DisponibilidadConsultaDTO resultado =
                mesaService.consultarDisponibilidad(
                        fecha, hora, duracion, cantidadPersonas, excluirReservaId);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para dar de alta una nueva mesa en el restaurante. Retorna HTTP 201 (Created) con la
     * mesa registrada.
     */
    @PostMapping
    public ResponseEntity<Mesa> crear(@Valid @RequestBody MesaDTO dto) {
        Mesa nueva = mesaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    /**
     * Endpoint para modificar la configuración de una mesa existente. Retorna HTTP 200 (OK) con la
     * mesa actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Mesa> actualizar(@PathVariable Long id, @Valid @RequestBody MesaDTO dto) {
        Mesa actualizada = mesaService.actualizar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Endpoint para eliminar una mesa del restaurante. Retorna HTTP 204 (No Content) si la
     * eliminación es exitosa y no tiene reservas activas.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
