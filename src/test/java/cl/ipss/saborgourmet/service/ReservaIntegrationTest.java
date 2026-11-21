package cl.ipss.saborgourmet.service;

import static org.assertj.core.api.Assertions.*;

import cl.ipss.saborgourmet.dto.*;
import cl.ipss.saborgourmet.exception.*;
import cl.ipss.saborgourmet.model.*;
import cl.ipss.saborgourmet.repository.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

/** Pruebas reales contra H2 y Spring Data JPA, con rollback independiente por caso. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservaIntegrationTest {
    @Autowired ReservaService reservas;
    @Autowired ClienteService clientes;
    @Autowired MesaService mesas;
    @Autowired ReservaRepository repository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired MesaRepository mesaRepository;
    Cliente cliente;
    Mesa mesa;
    LocalDate dia;

    @BeforeEach
    void preparar() {
        cliente =
                clientes.crear(
                        new ClienteDTO("Cliente Prueba", "prueba@example.test", "+56911112222"));
        mesa = mesas.crear(new MesaDTO(20, 4, "Terraza", true));
        dia = LocalDate.now(HorarioService.ZONA).plusDays(3);
    }

    ReservaRequestDTO solicitud(int hora, int minuto, int duracion, int personas) {
        return new ReservaRequestDTO(
                cliente.getId(),
                mesa.getId(),
                dia,
                LocalTime.of(hora, minuto),
                duracion,
                personas,
                "Prueba JPA");
    }

    @Test
    @DisplayName("Crear una reserva persiste las relaciones de cliente y mesa")
    void crear() {
        var creada = reservas.crear(solicitud(13, 0, 120, 3));
        assertThat(creada.getId()).isPositive();
        assertThat(repository.findById(creada.getId()).orElseThrow().getCliente().getId())
                .isEqualTo(cliente.getId());
        assertThat(creada.getEstado()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    @DisplayName("Actualizar permite cambiar horario y observaciones sin chocar consigo misma")
    void modificar() {
        var creada = reservas.crear(solicitud(13, 0, 120, 3));
        var cambio = solicitud(13, 30, 90, 2);
        cambio.setObservaciones("Ventanal");
        var modificada = reservas.modificar(creada.getId(), cambio);
        assertThat(modificada.getHora()).isEqualTo(LocalTime.of(13, 30));
        assertThat(modificada.getObservaciones()).isEqualTo("Ventanal");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Una reserva superpuesta se rechaza sin crear un segundo registro")
    void bloquearSolapamiento() {
        reservas.crear(solicitud(13, 0, 120, 3));
        assertThatThrownBy(() -> reservas.crear(solicitud(14, 0, 120, 2)))
                .isInstanceOf(ConflictException.class);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Los turnos contiguos pueden compartir mesa")
    void permitirContiguas() {
        reservas.crear(solicitud(13, 0, 120, 3));
        assertThat(reservas.crear(solicitud(15, 0, 120, 2)).getId()).isPositive();
    }

    @Test
    @DisplayName("Una reserva que cruza medianoche bloquea también el día siguiente")
    void cruzarMedianoche() {
        reservas.crear(solicitud(23, 0, 120, 2));
        var siguiente = solicitud(0, 30, 60, 2);
        siguiente.setFecha(dia.plusDays(1));
        assertThatThrownBy(() -> reservas.crear(siguiente)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Cancelar conserva la reserva y libera la disponibilidad")
    void cancelar() {
        var creada = reservas.crear(solicitud(13, 0, 120, 3));
        assertThat(
                        mesas.consultarDisponibilidad(dia, LocalTime.of(13, 0), 120, 2)
                                .getMesasDisponibles())
                .isEmpty();
        reservas.cancelar(creada.getId(), "Cambio de planes");
        assertThat(reservas.obtenerPorId(creada.getId()).getEstado())
                .isEqualTo(EstadoReserva.CANCELADA);
        assertThat(
                        mesas.consultarDisponibilidad(dia, LocalTime.of(13, 0), 120, 2)
                                .getMesasDisponibles())
                .hasSize(1);
    }

    @Test
    @DisplayName("Una reserva cancelada no se puede editar")
    void noEditarCancelada() {
        var creada = reservas.crear(solicitud(13, 0, 120, 3));
        reservas.cancelar(creada.getId(), null);
        assertThatThrownBy(() -> reservas.modificar(creada.getId(), solicitud(14, 0, 120, 2)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("La capacidad de mesa se comprueba en el servidor")
    void rechazarCapacidad() {
        assertThatThrownBy(() -> reservas.crear(solicitud(13, 0, 120, 5)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Las mesas inactivas no admiten reservas")
    void rechazarInactiva() {
        mesas.actualizar(mesa.getId(), new MesaDTO(20, 4, "Terraza", false));
        assertThatThrownBy(() -> reservas.crear(solicitud(13, 0, 120, 2)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("No es posible reservar con un cliente inexistente")
    void rechazarClienteInexistente() {
        var dto = solicitud(13, 0, 120, 2);
        dto.setClienteId(Long.MAX_VALUE);
        assertThatThrownBy(() -> reservas.crear(dto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("No es posible reservar una mesa inexistente")
    void rechazarMesaInexistente() {
        var dto = solicitud(13, 0, 120, 2);
        dto.setMesaId(Long.MAX_VALUE);
        assertThatThrownBy(() -> reservas.crear(dto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Se rechazan fechas pasadas")
    void rechazarPasado() {
        var dto = solicitud(13, 0, 120, 2);
        dto.setFecha(dia.minusDays(20));
        assertThatThrownBy(() -> reservas.crear(dto)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Se rechazan duraciones fuera del rango establecido")
    void rechazarDuracion() {
        assertThatThrownBy(() -> reservas.crear(solicitud(13, 0, -1, 2)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("La consulta de edición excluye la propia reserva")
    void disponibilidadAlEditar() {
        var r = reservas.crear(solicitud(13, 0, 120, 2));
        assertThat(
                        mesas.consultarDisponibilidad(dia, LocalTime.of(13, 0), 120, 2, r.getId())
                                .getMesasDisponibles())
                .hasSize(1);
    }

    @Test
    @DisplayName("Los filtros JPA devuelven solo las reservas coincidentes")
    void filtros() {
        var r = reservas.crear(solicitud(13, 0, 120, 2));
        assertThat(reservas.listar(dia, cliente.getId(), mesa.getId(), EstadoReserva.ACTIVA))
                .extracting(ReservaResponseDTO::getId)
                .containsExactly(r.getId());
        assertThat(reservas.listar(dia.plusDays(1), null, null, null)).isEmpty();
    }

    @Test
    @DisplayName("No se puede reducir capacidad por debajo de una reserva activa")
    void protegerCapacidad() {
        reservas.crear(solicitud(13, 0, 120, 4));
        assertThatThrownBy(
                        () -> mesas.actualizar(mesa.getId(), new MesaDTO(20, 2, "Terraza", true)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("No se desactiva una mesa con reservas futuras")
    void protegerMesaReservada() {
        reservas.crear(solicitud(13, 0, 120, 3));
        assertThatThrownBy(
                        () -> mesas.actualizar(mesa.getId(), new MesaDTO(20, 4, "Terraza", false)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("El historial impide eliminar al cliente incluso después de cancelar")
    void conservarHistorial() {
        var r = reservas.crear(solicitud(13, 0, 120, 2));
        reservas.cancelar(r.getId(), null);
        assertThatThrownBy(() -> clientes.eliminar(cliente.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Un correo duplicado se detecta ignorando mayúsculas y espacios")
    void correoDuplicado() {
        assertThatThrownBy(
                        () ->
                                clientes.crear(
                                        new ClienteDTO("Otro", " PRUEBA@EXAMPLE.TEST ", "123")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Un número de mesa duplicado se rechaza")
    void mesaDuplicada() {
        assertThatThrownBy(() -> mesas.crear(new MesaDTO(20, 6, "Salón", true)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Eliminar una reserva retira el registro sin borrar cliente ni mesa")
    void eliminarReserva() {
        var r = reservas.crear(solicitud(13, 0, 120, 2));
        reservas.eliminar(r.getId());
        assertThat(repository.existsById(r.getId())).isFalse();
        assertThat(clienteRepository.existsById(cliente.getId())).isTrue();
        assertThat(mesaRepository.existsById(mesa.getId())).isTrue();
    }

    @Test
    @DisplayName("Se puede completar CRUD de un cliente sin reservas")
    void crudCliente() {
        var actualizado =
                clientes.actualizar(
                        cliente.getId(),
                        new ClienteDTO("Nombre corregido", "nuevo@example.test", "456"));
        assertThat(actualizado.getNombre()).isEqualTo("Nombre corregido");
        clientes.eliminar(cliente.getId());
        assertThat(clienteRepository.existsById(cliente.getId())).isFalse();
    }

    @Test
    @DisplayName("Se puede completar CRUD de una mesa sin reservas")
    void crudMesa() {
        assertThat(mesas.actualizar(mesa.getId(), new MesaDTO(21, 6, "Salón", true)).getCapacidad())
                .isEqualTo(6);
        mesas.eliminar(mesa.getId());
        assertThat(mesaRepository.existsById(mesa.getId())).isFalse();
    }
}
