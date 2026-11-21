package cl.ipss.saborgourmet.model;

import jakarta.persistence.*;

import java.time.*;

/**
 * Reserva JPA con claves foráneas reales a Cliente y Mesa. Inicio y fin son fechas completas: un
 * turno que cruza medianoche también bloquea disponibilidad. La duración y la fecha visible se
 * calculan desde esos valores, sin duplicarlos en columnas.
 */
@Entity
@Table(
        name = "reservas",
        indexes =
                @Index(
                        name = "ix_reserva_mesa_intervalo",
                        columnList = "mesa_id,estado,inicio,fin"))
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cliente_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reserva_cliente"))
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "mesa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reserva_mesa"))
    private Mesa mesa;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    @Column(nullable = false)
    private Integer cantidadPersonas;

    @Column(length = 1000)
    private String observaciones;

    public Reserva() {}

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Long getClienteId() {
        return cliente.getId();
    }

    public Long getMesaId() {
        return mesa.getId();
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public LocalDate getFecha() {
        return inicio.toLocalDate();
    }

    public LocalTime getHora() {
        return inicio.toLocalTime();
    }

    public LocalTime getHoraFin() {
        return fin.toLocalTime();
    }

    public Integer getDuracionMinutos() {
        return Math.toIntExact(Duration.between(inicio, fin).toMinutes());
    }

    public void definirHorario(LocalDate fecha, LocalTime hora, int duracion) {
        this.inicio = fecha.atTime(hora);
        this.fin = inicio.plusMinutes(duracion);
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
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
