package cl.ipss.saborgourmet.web;

import cl.ipss.saborgourmet.dto.ReservaResponseDTO;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.service.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;
import java.util.Comparator;

/** Compone el panel de inicio con datos del servicio, sin acceder a los repositorios. */
@Controller
public class InicioController {
    private final ReservaService reservas;
    private final MesaService mesas;
    private final ClienteService clientes;

    public InicioController(ReservaService reservas, MesaService mesas, ClienteService clientes) {
        this.reservas = reservas;
        this.mesas = mesas;
        this.clientes = clientes;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        LocalDate hoy = LocalDate.now(HorarioService.ZONA);
        var todas = reservas.listar(null, null, null, null);
        var proximas =
                todas.stream()
                        .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                        .filter(
                                r ->
                                        r.getFecha()
                                                .atTime(r.getHora())
                                                .isAfter(LocalDateTime.now(HorarioService.ZONA)))
                        .sorted(
                                Comparator.comparing(ReservaResponseDTO::getFecha)
                                        .thenComparing(ReservaResponseDTO::getHora))
                        .toList();
        model.addAttribute("hoy", hoy);
        model.addAttribute(
                "reservasHoy",
                todas.stream()
                        .filter(
                                r ->
                                        r.getFecha().equals(hoy)
                                                && r.getEstado() == EstadoReserva.ACTIVA)
                        .count());
        model.addAttribute("activas", proximas.size());
        model.addAttribute(
                "mesasActivas", mesas.listarTodas().stream().filter(m -> m.getActiva()).count());
        model.addAttribute("totalClientes", clientes.listarTodos().size());
        model.addAttribute("proximas", proximas.stream().limit(5).toList());
        return "inicio";
    }
}
