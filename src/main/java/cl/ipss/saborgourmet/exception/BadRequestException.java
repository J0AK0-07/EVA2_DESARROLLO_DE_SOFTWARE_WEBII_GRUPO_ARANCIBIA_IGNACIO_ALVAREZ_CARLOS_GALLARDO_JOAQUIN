package cl.ipss.saborgourmet.exception;

/**
 * Excepción lanzada cuando los datos de la solicitud son inválidos o no cumplen con las reglas de
 * negocio. Ejemplos: - Campos obligatorios faltantes o vacíos. - Cantidad de personas o capacidad
 * menor o igual a cero. - Capacidad de la mesa insuficiente para la cantidad de personas
 * solicitada. - Fechas en el pasado para nuevas reservas.
 *
 * <p>Se traduce a un estado HTTP 400 (Bad Request).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
