package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.exception.BadRequestException;

import org.springframework.stereotype.Service;

import java.time.*;

/** Reglas temporales compartidas por la disponibilidad y por la creación de reservas. */
@Service
public class HorarioService {
    public static final ZoneId ZONA = ZoneId.of("America/Santiago");

    public int validar(LocalDate fecha, LocalTime hora, Integer duracion, Integer personas) {
        if (fecha == null || hora == null)
            throw new BadRequestException("Indique fecha y hora de la reserva.");
        if (personas == null || personas < 1 || personas > 100)
            throw new BadRequestException("La cantidad de personas debe estar entre 1 y 100.");
        int minutos = duracion == null ? 120 : duracion;
        if (minutos < 15 || minutos > 480)
            throw new BadRequestException("La duración debe estar entre 15 y 480 minutos.");
        if (!fecha.atTime(hora).isAfter(LocalDateTime.now(ZONA)))
            throw new BadRequestException("Seleccione una fecha y hora futuras.");
        return minutos;
    }
}
