package cl.ipss.saborgourmet.config;

import cl.ipss.saborgourmet.dto.*;
import cl.ipss.saborgourmet.repository.*;
import cl.ipss.saborgourmet.service.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

/** Datos ficticios para demostrar el sistema. Solo se crean si la base está completamente vacía. */
@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements CommandLineRunner {
    private final ClienteRepository clientes;
    private final MesaRepository mesas;
    private final ReservaRepository reservas;
    private final ClienteService clienteService;
    private final MesaService mesaService;
    private final ReservaService reservaService;

    public DemoDataInitializer(
            ClienteRepository clientes,
            MesaRepository mesas,
            ReservaRepository reservas,
            ClienteService clienteService,
            MesaService mesaService,
            ReservaService reservaService) {
        this.clientes = clientes;
        this.mesas = mesas;
        this.reservas = reservas;
        this.clienteService = clienteService;
        this.mesaService = mesaService;
        this.reservaService = reservaService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (clientes.count() != 0 || mesas.count() != 0 || reservas.count() != 0) return;
        var alicia =
                clienteService.crear(
                        new ClienteDTO("Alicia Vera", "alicia@example.test", "+56 9 1111 2222"));
        var martin =
                clienteService.crear(
                        new ClienteDTO("Martín Soto", "martin@example.test", "+56 9 3333 4444"));
        clienteService.crear(
                new ClienteDTO("Valentina Rojas", "valentina@example.test", "+56 9 5555 6666"));
        mesaService.crear(new MesaDTO(1, 2, "Terraza", true));
        var salon = mesaService.crear(new MesaDTO(2, 4, "Salón principal", true));
        mesaService.crear(new MesaDTO(3, 4, "Ventanal", true));
        var vip = mesaService.crear(new MesaDTO(4, 6, "Salón privado", true));
        mesaService.crear(new MesaDTO(5, 8, "Salón familiar", true));
        LocalDate dia = LocalDate.now(HorarioService.ZONA).plusDays(1);
        reservaService.crear(
                new ReservaRequestDTO(
                        alicia.getId(),
                        salon.getId(),
                        dia,
                        LocalTime.of(13, 0),
                        120,
                        3,
                        "Almuerzo de trabajo"));
        reservaService.crear(
                new ReservaRequestDTO(
                        martin.getId(),
                        vip.getId(),
                        dia,
                        LocalTime.of(20, 30),
                        120,
                        5,
                        "Celebración familiar"));
    }
}
