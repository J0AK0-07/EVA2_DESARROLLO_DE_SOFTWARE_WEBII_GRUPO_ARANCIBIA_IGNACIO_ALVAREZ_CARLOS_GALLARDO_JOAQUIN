package cl.ipss.saborgourmet.model;

import jakarta.persistence.*;

/** Configuración de una mesa. Una mesa inactiva no admite nuevas reservas. */
@Entity
@Table(
        name = "mesas",
        uniqueConstraints = @UniqueConstraint(name = "uk_mesa_numero", columnNames = "numero"))
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false, length = 100)
    private String ubicacion;

    @Column(nullable = false)
    private Boolean activa = true;

    public Mesa() {}

    public Mesa(Long id, Integer numero, Integer capacidad, String ubicacion, Boolean activa) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
        this.activa = activa;
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
