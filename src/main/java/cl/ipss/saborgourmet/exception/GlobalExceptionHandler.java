package cl.ipss.saborgourmet.exception;

import cl.ipss.saborgourmet.dto.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controlador global de excepciones para la API REST del Restaurante "Sabor Gourmet". Centraliza el
 * manejo de errores, intercepta las excepciones del negocio y traduce cada caso a un código de
 * respuesta HTTP coherente (400, 404, 409, 500) en formato JSON, evitando exponer trazas internas.
 */
@RestControllerAdvice(basePackages = "cl.ipss.saborgourmet.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de formulario JSON y parámetros se informan como 400, nunca como 500. */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String mensaje =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .distinct()
                        .collect(java.util.stream.Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "Bad Request", mensaje, request.getRequestURI()));
    }

    @ExceptionHandler({
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleParameter(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(
                        new ErrorResponseDTO(
                                400,
                                "Bad Request",
                                "Verifique los parámetros: fecha YYYY-MM-DD, hora HH:mm e"
                                    + " identificadores numéricos.",
                                request.getRequestURI()));
    }

    @ExceptionHandler({
        org.springframework.dao.DataIntegrityViolationException.class,
        org.springframework.dao.ConcurrencyFailureException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleDatabaseConflict(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(409)
                .body(
                        new ErrorResponseDTO(
                                409,
                                "Conflict",
                                "La operación entra en conflicto con otro registro o solicitud."
                                    + " Actualice la información e intente nuevamente.",
                                request.getRequestURI()));
    }

    /** Maneja excepciones de recursos no encontrados (HTTP 404 Not Found). */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {} en {}", ex.getMessage(), request.getRequestURI());
        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja conflictos de estado o negocio como reservas superpuestas o duplicados (HTTP 409
     * Conflict).
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(
            ConflictException ex, HttpServletRequest request) {
        log.warn("Conflicto de negocio: {} en {}", ex.getMessage(), request.getRequestURI());
        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja errores de validación de datos de entrada o parámetros inválidos (HTTP 400 Bad
     * Request).
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {
        log.warn("Solicitud inválida: {} en {}", ex.getMessage(), request.getRequestURI());
        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /** Maneja excepciones de argumentos ilegales (HTTP 400 Bad Request). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argumento inválido: {} en {}", ex.getMessage(), request.getRequestURI());
        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja errores de deserialización JSON, formatos de fecha incorrectos o cuerpos mal formados
     * (HTTP 400 Bad Request).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Cuerpo JSON mal formado o formato inválido en {}", request.getRequestURI());
        String mensaje =
                "El cuerpo de la solicitud JSON contiene errores de sintaxis o tipos de datos"
                    + " inválidos.";
        if (ex.getMessage() != null && ex.getMessage().contains("LocalDate")) {
            mensaje =
                    "El formato de fecha es inválido. Utilice el formato estándar ISO 'YYYY-MM-DD'"
                        + " (ej. 2026-11-20).";
        } else if (ex.getMessage() != null && ex.getMessage().contains("LocalTime")) {
            mensaje =
                    "El formato de hora es inválido. Utilice el formato estándar 'HH:mm' (ej."
                        + " 13:30).";
        }
        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        mensaje,
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja cualquier excepción no controlada del sistema (HTTP 500 Internal Server Error)
     * protegiendo la seguridad sin filtrar trazas internas ni detalles del servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Error interno no controlado en {}: ", request.getRequestURI(), ex);
        ErrorResponseDTO error =
                new ErrorResponseDTO(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Ha ocurrido un error interno en el servidor. Por favor, intente más"
                            + " tarde.",
                        request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
