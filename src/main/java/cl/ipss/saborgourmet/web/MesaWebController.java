package cl.ipss.saborgourmet.web;

import cl.ipss.saborgourmet.dto.MesaDTO;
import cl.ipss.saborgourmet.exception.*;
import cl.ipss.saborgourmet.service.MesaService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Administración de número, ubicación, capacidad y estado de mesas desde Thymeleaf. */
@Controller
public class MesaWebController {
    private final MesaService mesas;

    public MesaWebController(MesaService mesas) {
        this.mesas = mesas;
    }

    @GetMapping("/mesas")
    public String listar(Model model) {
        model.addAttribute("mesas", mesas.listarTodas());
        return "mesas/lista";
    }

    @GetMapping("/mesas/nueva")
    public String nueva(Model model) {
        model.addAttribute("form", new MesaDTO());
        return "mesas/form";
    }

    @GetMapping("/mesas/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        var m = mesas.obtenerPorId(id);
        model.addAttribute(
                "form",
                new MesaDTO(
                        m.getId(),
                        m.getNumero(),
                        m.getCapacidad(),
                        m.getUbicacion(),
                        m.getActiva()));
        model.addAttribute("mesaId", id);
        return "mesas/form";
    }

    @PostMapping({"/mesas", "/mesas/{id}"})
    public String guardar(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") MesaDTO form,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                if (id == null) mesas.crear(form);
                else mesas.actualizar(id, form);
                flash.addFlashAttribute("exito", "Configuración de mesa guardada correctamente.");
                return "redirect:/mesas";
            } catch (BadRequestException | ConflictException | ResourceNotFoundException ex) {
                result.reject("negocio", ex.getMessage());
            }
        }
        model.addAttribute("mesaId", id);
        return "mesas/form";
    }

    @PostMapping("/mesas/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            mesas.eliminar(id);
            flash.addFlashAttribute("exito", "Mesa eliminada correctamente.");
        } catch (BadRequestException | ConflictException | ResourceNotFoundException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/mesas";
    }
}
