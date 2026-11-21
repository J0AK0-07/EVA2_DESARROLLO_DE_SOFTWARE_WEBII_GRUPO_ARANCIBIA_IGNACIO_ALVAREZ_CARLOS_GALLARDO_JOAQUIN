package cl.ipss.saborgourmet.web;

import cl.ipss.saborgourmet.dto.ReservaRequestDTO;
import cl.ipss.saborgourmet.exception.*;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.service.*;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.util.List;

/** Formularios Thymeleaf de reserva. Los errores conservan los datos ingresados por el usuario. */
@Controller
public class ReservaWebController {
    private final ReservaService reservas;
    private final MesaService mesas;
    private final ClienteService clientes;

    public ReservaWebController(
            ReservaService reservas, MesaService mesas, ClienteService clientes) {
        this.reservas = reservas;
        this.mesas = mesas;
        this.clientes = clientes;
    }

    @GetMapping("/reservas")
    public String listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fecha,
            @RequestParam(required = false) EstadoReserva estado,
            Model model) {
        model.addAttribute("reservas", reservas.listar(fecha, null, null, estado));
        model.addAttribute("fecha", fecha);
        model.addAttribute("estado", estado);
        return "reservas/lista";
    }

    @GetMapping("/disponibilidad")
    public String disponibilidad(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(defaultValue = "2") Integer personas,
            @RequestParam(defaultValue = "120") Integer duracion,
            Model model) {
        fecha = fecha == null ? LocalDate.now(HorarioService.ZONA).plusDays(1) : fecha;
        hora = hora == null ? LocalTime.of(13, 0) : hora;
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", hora);
        model.addAttribute("personas", personas);
        model.addAttribute("duracion", duracion);
        try {
            model.addAttribute(
                    "consulta", mesas.consultarDisponibilidad(fecha, hora, duracion, personas));
        } catch (BadRequestException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "disponibilidad";
    }

    @GetMapping("/reservas/nueva")
    public String nueva(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(defaultValue = "2") Integer personas,
            @RequestParam(defaultValue = "120") Integer duracion,
            @RequestParam(required = false) Long mesaId,
            Model model) {
        var dto =
                new ReservaRequestDTO(
                        null,
                        mesaId,
                        fecha == null ? LocalDate.now(HorarioService.ZONA).plusDays(1) : fecha,
                        hora == null ? LocalTime.of(13, 0) : hora,
                        duracion,
                        personas,
                        "");
        model.addAttribute("form", dto);
        preparar(model, dto, null);
        return "reservas/form";
    }

    @GetMapping("/reservas/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        var r = reservas.obtenerPorId(id);
        if (r.getEstado() != EstadoReserva.ACTIVA)
            throw new BadRequestException("Solo se pueden editar reservas activas.");
        var dto =
                new ReservaRequestDTO(
                        r.getClienteId(),
                        r.getMesaId(),
                        r.getFecha(),
                        r.getHora(),
                        r.getDuracionMinutos(),
                        r.getCantidadPersonas(),
                        r.getObservaciones());
        model.addAttribute("form", dto);
        preparar(model, dto, id);
        return "reservas/form";
    }

    @PostMapping({"/reservas", "/reservas/{id}"})
    public String guardar(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") ReservaRequestDTO form,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                if (id == null) reservas.crear(form);
                else reservas.modificar(id, form);
                flash.addFlashAttribute(
                        "exito",
                        id == null
                                ? "Reserva creada correctamente."
                                : "Reserva actualizada correctamente.");
                return "redirect:/reservas";
            } catch (BadRequestException | ConflictException | ResourceNotFoundException ex) {
                result.reject("negocio", ex.getMessage());
            }
        }
        preparar(model, form, id);
        return "reservas/form";
    }

    @PostMapping("/reservas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            reservas.cancelar(id, null);
            flash.addFlashAttribute(
                    "exito", "Reserva cancelada. La mesa vuelve a estar disponible.");
        } catch (BadRequestException | ConflictException | ResourceNotFoundException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/reservas";
    }

    @PostMapping("/reservas/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            reservas.eliminar(id);
            flash.addFlashAttribute("exito", "Reserva eliminada del historial.");
        } catch (BadRequestException | ConflictException | ResourceNotFoundException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/reservas";
    }

    private void preparar(Model model, ReservaRequestDTO form, Long id) {
        model.addAttribute("reservaId", id);
        model.addAttribute("clientes", clientes.listarTodos());
        model.addAttribute("candidatas", List.of());
        try {
            model.addAttribute(
                    "candidatas",
                    mesas.consultarDisponibilidad(
                                    form.getFecha(),
                                    form.getHora(),
                                    form.getDuracionMinutos(),
                                    form.getCantidadPersonas(),
                                    id)
                            .getMesasDisponibles());
        } catch (BadRequestException ignored) {
            /* La vista muestra la validación y permite cambiar el horario. */
        }
    }
}
