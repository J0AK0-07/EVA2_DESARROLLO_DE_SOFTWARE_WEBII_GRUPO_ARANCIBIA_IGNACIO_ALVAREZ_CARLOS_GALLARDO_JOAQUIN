package cl.ipss.saborgourmet.exception;

/**
 * Excepción lanzada cuando un recurso solicitado (Cliente, Mesa o Reserva) no existe en el sistema.
 * El GlobalExceptionHandler la traduce a un estado HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
