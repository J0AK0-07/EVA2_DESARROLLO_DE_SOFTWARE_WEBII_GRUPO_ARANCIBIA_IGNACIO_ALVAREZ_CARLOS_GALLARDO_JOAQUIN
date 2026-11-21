package cl.ipss.saborgourmet.web;

import cl.ipss.saborgourmet.dto.ClienteDTO;
import cl.ipss.saborgourmet.exception.*;
import cl.ipss.saborgourmet.service.ClienteService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Interfaz MVC de clientes. Todas las escrituras delegan en el servicio compartido con la API. */
@Controller
public class ClienteWebController {
    private final ClienteService clientes;

    public ClienteWebController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @GetMapping("/clientes")
    public String listar(Model model) {
        model.addAttribute("clientes", clientes.listarTodos());
        return "clientes/lista";
    }

    @GetMapping("/clientes/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new ClienteDTO());
        return "clientes/form";
    }

    @GetMapping("/clientes/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        var c = clientes.obtenerPorId(id);
        model.addAttribute(
                "form", new ClienteDTO(c.getId(), c.getNombre(), c.getEmail(), c.getTelefono()));
        model.addAttribute("clienteId", id);
        return "clientes/form";
    }

    @PostMapping({"/clientes", "/clientes/{id}"})
    public String guardar(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") ClienteDTO form,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                if (id == null) clientes.crear(form);
                else clientes.actualizar(id, form);
                flash.addFlashAttribute("exito", "Datos del cliente guardados correctamente.");
                return "redirect:/clientes";
            } catch (BadRequestException | ConflictException | ResourceNotFoundException ex) {
                result.reject("negocio", ex.getMessage());
            }
        }
        model.addAttribute("clienteId", id);
        return "clientes/form";
    }

    @PostMapping("/clientes/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            clientes.eliminar(id);
            flash.addFlashAttribute("exito", "Cliente eliminado correctamente.");
        } catch (BadRequestException | ConflictException | ResourceNotFoundException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/clientes";
    }
}
