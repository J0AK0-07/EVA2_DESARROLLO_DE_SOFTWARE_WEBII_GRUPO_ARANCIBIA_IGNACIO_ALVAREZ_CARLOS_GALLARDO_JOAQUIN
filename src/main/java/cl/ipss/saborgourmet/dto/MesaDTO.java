package cl.ipss.saborgourmet.dto;

import jakarta.validation.constraints.*;

/** DTO para la transferencia de información y configuración de Mesas. */
public class MesaDTO {

    private Long id;

    @NotNull(message = "Ingrese el número de mesa.")
    @Min(1)
    @Max(9999)
    private Integer numero;

    @NotNull(message = "Ingrese la capacidad.")
    @Min(1)
    @Max(100)
    private Integer capacidad;

    @Size(max = 100)
    private String ubicacion;

    private Boolean activa = true;

    public MesaDTO(Integer numero, Integer capacidad, String ubicacion, Boolean activa) {
        this(null, numero, capacidad, ubicacion, activa);
    }

    public MesaDTO() {}

    public MesaDTO(Long id, Integer numero, Integer capacidad, String ubicacion, Boolean activa) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
        this.activa = (activa != null) ? activa : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
