package cl.ipss.saborgourmet.dto;

import cl.ipss.saborgourmet.model.Mesa;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** DTO que resume los resultados de una consulta de disponibilidad de mesas. */
public class DisponibilidadConsultaDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private Integer duracionMinutos;
    private Integer cantidadPersonas;
    private int totalMesasDisponibles;
    private List<Mesa> mesasDisponibles;

    public DisponibilidadConsultaDTO() {}

    public DisponibilidadConsultaDTO(
            LocalDate fecha,
            LocalTime hora,
            Integer duracionMinutos,
            Integer cantidadPersonas,
            List<Mesa> mesasDisponibles) {
        this.fecha = fecha;
        this.hora = hora;
        this.duracionMinutos = (duracionMinutos != null) ? duracionMinutos : 120;
        this.horaFin = (hora != null) ? hora.plusMinutes(this.duracionMinutos) : null;
        this.cantidadPersonas = cantidadPersonas;
        this.mesasDisponibles = mesasDisponibles;
        this.totalMesasDisponibles = (mesasDisponibles != null) ? mesasDisponibles.size() : 0;
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

    public int getTotalMesasDisponibles() {
        return totalMesasDisponibles;
    }

    public void setTotalMesasDisponibles(int totalMesasDisponibles) {
        this.totalMesasDisponibles = totalMesasDisponibles;
    }

    public List<Mesa> getMesasDisponibles() {
        return mesasDisponibles;
    }

    public void setMesasDisponibles(List<Mesa> mesasDisponibles) {
        this.mesasDisponibles = mesasDisponibles;
        this.totalMesasDisponibles = (mesasDisponibles != null) ? mesasDisponibles.size() : 0;
    }
}
