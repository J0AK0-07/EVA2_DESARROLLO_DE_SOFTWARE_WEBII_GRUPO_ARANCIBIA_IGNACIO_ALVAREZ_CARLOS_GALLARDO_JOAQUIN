package cl.ipss.saborgourmet.service;

import static org.assertj.core.api.Assertions.assertThat;

import cl.ipss.saborgourmet.dto.*;
import cl.ipss.saborgourmet.exception.ConflictException;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Dos transacciones y clientes diferentes compiten realmente por la misma mesa. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConcurrenciaIntegrationTest {
    @Autowired ClienteService clientes;
    @Autowired MesaService mesas;
    @Autowired ReservaService reservas;

    @Test
    @DisplayName("Dos reservas simultáneas producen exactamente un éxito y un conflicto")
    void evitarDobleReserva() throws Exception {
        var a = clientes.crear(new ClienteDTO("Cliente A", "a@example.test", "123"));
        var b = clientes.crear(new ClienteDTO("Cliente B", "b@example.test", "456"));
        var mesa = mesas.crear(new MesaDTO(500, 4, "Prueba concurrente", true));
        LocalDate dia = LocalDate.now(HorarioService.ZONA).plusDays(4);
        var listos = new CountDownLatch(2);
        var salida = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<String>> resultados = new ArrayList<>();
            for (Long clienteId : List.of(a.getId(), b.getId()))
                resultados.add(
                        pool.submit(
                                () -> {
                                    listos.countDown();
                                    salida.await(10, TimeUnit.SECONDS);
                                    try {
                                        reservas.crear(
                                                new ReservaRequestDTO(
                                                        clienteId,
                                                        mesa.getId(),
                                                        dia,
                                                        LocalTime.of(13, 0),
                                                        120,
                                                        2,
                                                        "Concurrencia"));
                                        return "CREADA";
                                    } catch (ConflictException ex) {
                                        return "CONFLICTO";
                                    }
                                }));
            assertThat(listos.await(10, TimeUnit.SECONDS)).isTrue();
            salida.countDown();
            assertThat(
                            List.of(
                                    resultados.get(0).get(20, TimeUnit.SECONDS),
                                    resultados.get(1).get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("CREADA", "CONFLICTO");
        } finally {
            salida.countDown();
            pool.shutdownNow();
        }
    }
}
