package cl.ipss.saborgourmet.exception;

/**
 * Excepción lanzada cuando ocurre un conflicto de negocio o estado. Ejemplos: - Intentar reservar
 * una mesa que ya está ocupada en ese turno/horario. - Intentar registrar una mesa con un número
 * físico duplicado. - Intentar registrar un cliente con un correo electrónico ya existente. -
 * Intentar eliminar un cliente o mesa que posee reservas activas vinculadas.
 *
 * <p>Se traduce a un estado HTTP 409 (Conflict).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
