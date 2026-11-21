package cl.ipss.saborgourmet.dto;

import cl.ipss.saborgourmet.model.EstadoReserva;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO enriquecido para las respuestas de Reservas hacia el cliente de la API. Proporciona tanto los
 * identificadores de referencia como los datos contextuales del Cliente y de la Mesa.
 */
public class ReservaResponseDTO {

    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private Long mesaId;
    private Integer mesaNumero;
    private Integer mesaCapacidad;
    private String mesaUbicacion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private Integer duracionMinutos;
    private Integer cantidadPersonas;
    private EstadoReserva estado;
    private String observaciones;

    public ReservaResponseDTO() {}

    /** Fecha de término; permite mostrar reservas que continúan al día siguiente. */
    public LocalDate getFechaFin() {
        return fecha.atTime(hora).plusMinutes(duracionMinutos).toLocalDate();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public Integer getMesaNumero() {
        return mesaNumero;
    }

    public void setMesaNumero(Integer mesaNumero) {
        this.mesaNumero = mesaNumero;
    }

    public Integer getMesaCapacidad() {
        return mesaCapacidad;
    }

    public void setMesaCapacidad(Integer mesaCapacidad) {
        this.mesaCapacidad = mesaCapacidad;
    }

    public String getMesaUbicacion() {
        return mesaUbicacion;
    }

    public void setMesaUbicacion(String mesaUbicacion) {
        this.mesaUbicacion = mesaUbicacion;
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

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
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

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
