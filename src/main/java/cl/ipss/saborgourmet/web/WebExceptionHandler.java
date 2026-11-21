package cl.ipss.saborgourmet.web;

import cl.ipss.saborgourmet.exception.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/** Los errores de las páginas se muestran en HTML; los de la API conservan su formato JSON. */
@ControllerAdvice(basePackages = "cl.ipss.saborgourmet.web")
public class WebExceptionHandler {
    @ModelAttribute("fechaMinima")
    public java.time.LocalDate fechaMinima() {
        return java.time.LocalDate.now(cl.ipss.saborgourmet.service.HorarioService.ZONA);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView noEncontrado(ResourceNotFoundException ex) {
        return pagina(404, ex.getMessage());
    }

    @ExceptionHandler({
        BadRequestException.class,
        org.springframework.validation.BindException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public ModelAndView invalido(Exception ex) {
        return pagina(400, "Verifique los datos o el formato de la dirección solicitada.");
    }

    @ExceptionHandler({
        ConflictException.class,
        org.springframework.dao.DataIntegrityViolationException.class,
        org.springframework.dao.ConcurrencyFailureException.class
    })
    public ModelAndView conflicto(Exception ex) {
        return pagina(
                409,
                "La operación entra en conflicto con otro registro. Actualice la página e intente"
                    + " nuevamente.");
    }

    private ModelAndView pagina(int status, String mensaje) {
        ModelAndView vista = new ModelAndView("error");
        vista.setStatus(org.springframework.http.HttpStatus.valueOf(status));
        vista.addObject("status", status);
        vista.addObject("mensaje", mensaje);
        return vista;
    }
}
