package cl.ipss.saborgourmet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para la creación y modificación de Reservas. Contiene los datos requeridos que envía el
 * cliente o recepcionista.
 */
public class ReservaRequestDTO {

    /** Identificador del cliente que realiza la reserva */
    @NotNull(message = "Seleccione un cliente.")
    @Positive
    private Long clienteId;

    /** Identificador de la mesa seleccionada */
    @NotNull(message = "Seleccione una mesa disponible.")
    @Positive
    private Long mesaId;

    /** Fecha en que se desea reservar (formato YYYY-MM-DD) */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Seleccione la fecha.")
    @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    /** Hora de inicio de la reserva (formato HH:mm) */
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "Seleccione la hora.")
    @org.springframework.format.annotation.DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    /** Duración del turno en minutos (opcional, por defecto 120 min) */
    @Min(value = 15, message = "Mínimo 15 minutos.")
    @Max(value = 480, message = "Máximo 480 minutos.")
    private Integer duracionMinutos = 120;

    /** Cantidad de personas comensales */
    @NotNull(message = "Ingrese la cantidad de personas.")
    @Min(1)
    @Max(100)
    private Integer cantidadPersonas;

    /** Notas especiales, alergias o solicitudes del cliente */
    @Size(max = 1000, message = "Máximo 1000 caracteres.")
    private String observaciones;

    public ReservaRequestDTO() {}

    public ReservaRequestDTO(
            Long clienteId,
            Long mesaId,
            LocalDate fecha,
            LocalTime hora,
            Integer duracionMinutos,
            Integer cantidadPersonas,
            String observaciones) {
        this.clienteId = clienteId;
        this.mesaId = mesaId;
        this.fecha = fecha;
        this.hora = hora;
        this.duracionMinutos = duracionMinutos != null ? duracionMinutos : 120;
        this.cantidadPersonas = cantidadPersonas;
        this.observaciones = observaciones;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public Integer getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(Integer cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
