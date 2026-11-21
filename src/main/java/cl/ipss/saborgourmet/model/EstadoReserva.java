package cl.ipss.saborgourmet.model;

/**
 * Enumeración que representa los estados posibles de una reserva en el Restaurante "Sabor Gourmet".
 *
 * <p>- ACTIVA: La reserva está confirmada y bloquea la disponibilidad de la mesa en el horario
 * correspondiente. - CANCELADA: La reserva fue anulada y libera la mesa para nuevas reservas. -
 * COMPLETADA: La reserva fue atendida con éxito cuando los clientes ya se presentaron.
 */
public enum EstadoReserva {
    ACTIVA,
    CANCELADA,
    COMPLETADA
}
