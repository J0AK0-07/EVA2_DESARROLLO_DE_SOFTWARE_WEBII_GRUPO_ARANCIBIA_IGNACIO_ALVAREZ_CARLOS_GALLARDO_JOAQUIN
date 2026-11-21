package cl.ipss.saborgourmet.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cl.ipss.saborgourmet.dto.*;
import cl.ipss.saborgourmet.model.*;
import cl.ipss.saborgourmet.service.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

/** Comprueba respuestas HTTP y renderizado real de Thymeleaf con un contexto Spring completo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiAndViewsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ClienteService clientes;
    @Autowired MesaService mesas;
    @Autowired ReservaService reservas;
    Cliente cliente;
    Mesa mesa;
    LocalDate dia;

    @BeforeEach
    void preparar() {
        cliente = clientes.crear(new ClienteDTO("Prueba MVC", "mvc@example.test", "12345"));
        mesa = mesas.crear(new MesaDTO(90, 4, "Ventanal", true));
        dia = LocalDate.now(HorarioService.ZONA).plusDays(5);
    }

    ReservaRequestDTO dto() {
        return new ReservaRequestDTO(
                cliente.getId(), mesa.getId(), dia, LocalTime.of(13, 0), 120, 2, "Prueba");
    }

    @Test
    @DisplayName("GET de las páginas principales renderiza HTML y recursos locales")
    void paginas() throws Exception {
        for (String ruta :
                new String[] {
                    "/",
                    "/disponibilidad",
                    "/reservas",
                    "/clientes",
                    "/mesas",
                    "/reservas/nueva",
                    "/clientes/nuevo",
                    "/mesas/nueva"
                })
            mvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/html"))
                    .andExpect(content().string(containsString("/css/app.css")));
    }

    @Test
    @DisplayName("Los formularios de edición de las tres entidades renderizan sus datos")
    void formulariosEdicion() throws Exception {
        var r = reservas.crear(dto());
        for (String ruta :
                new String[] {
                    "/clientes/" + cliente.getId() + "/editar",
                    "/mesas/" + mesa.getId() + "/editar",
                    "/reservas/" + r.getId() + "/editar"
                })
            mvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Guardar")));
    }

    @Test
    @DisplayName("La API crea una reserva y devuelve HTTP 201 con identificador")
    void crearReservaApi() throws Exception {
        mvc.perform(
                        post("/api/reservas")
                                .contentType("application/json")
                                .content(json.writeValueAsString(dto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.clienteNombre").value("Prueba MVC"));
    }

    @Test
    @DisplayName("Un conflicto de reserva devuelve HTTP 409")
    void conflictoApi() throws Exception {
        reservas.crear(dto());
        mvc.perform(
                        post("/api/reservas")
                                .contentType("application/json")
                                .content(json.writeValueAsString(dto())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Cancelar por API devuelve estado CANCELADA")
    void cancelarApi() throws Exception {
        var r = reservas.crear(dto());
        mvc.perform(patch("/api/reservas/" + r.getId() + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("Los cuerpos inválidos reciben HTTP 400 con mensaje de validación")
    void validarApi() throws Exception {
        mvc.perform(
                        post("/api/clientes")
                                .contentType("application/json")
                                .content("{\"nombre\":\"\",\"email\":\"mal\",\"telefono\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Las fechas mal formadas de la API reciben HTTP 400")
    void parametrosInvalidos() throws Exception {
        mvc.perform(
                        get("/api/mesas/disponibles")
                                .param("fecha", "incorrecta")
                                .param("hora", "13:00")
                                .param("personas", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Los recursos inexistentes devuelven HTTP 404")
    void recursoInexistente() throws Exception {
        mvc.perform(get("/api/reservas/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Un formulario inválido conserva los datos y muestra validaciones")
    void validarFormulario() throws Exception {
        mvc.perform(
                        post("/clientes")
                                .param("nombre", "Nombre conservado")
                                .param("email", "mal")
                                .param("telefono", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/form"))
                .andExpect(model().attributeHasFieldErrors("form", "email"))
                .andExpect(content().string(containsString("Nombre conservado")));
    }

    @Test
    @DisplayName("El formulario MVC crea una reserva y redirige a la agenda")
    void crearDesdeVista() throws Exception {
        mvc.perform(
                        post("/reservas")
                                .param("clienteId", cliente.getId().toString())
                                .param("mesaId", mesa.getId().toString())
                                .param("fecha", dia.toString())
                                .param("hora", "13:00")
                                .param("duracionMinutos", "120")
                                .param("cantidadPersonas", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attribute("exito", "Reserva creada correctamente."));
    }

    @Test
    @DisplayName("La vista de disponibilidad muestra mesas disponibles")
    void disponibilidadVista() throws Exception {
        mvc.perform(
                        get("/disponibilidad")
                                .param("fecha", dia.toString())
                                .param("hora", "13:00")
                                .param("personas", "2")
                                .param("duracion", "120"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reservar esta mesa")));
    }

    @Test
    @DisplayName("La vista de error HTML responde 404 sin exponer trazas")
    void errorVista() throws Exception {
        mvc.perform(get("/reservas/999999/editar"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Volver al inicio")));
    }
}
