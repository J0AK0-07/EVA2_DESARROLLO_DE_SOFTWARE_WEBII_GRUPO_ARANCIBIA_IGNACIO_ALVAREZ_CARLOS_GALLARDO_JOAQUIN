package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.ClienteDTO;
import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.service.ClienteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Clientes en el Restaurante "Sabor Gourmet". Expone los
 * endpoints HTTP para realizar operaciones CRUD completas. Utiliza ResponseEntity para retornar los
 * códigos de estado HTTP apropiados (200, 201, 204).
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Endpoint para listar todos los clientes registrados. Retorna HTTP 200 (OK) con la lista de
     * clientes en formato JSON.
     */
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Endpoint para consultar un cliente específico por su ID. Retorna HTTP 200 (OK) o 404 (Not
     * Found) a través del GlobalExceptionHandler.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Endpoint para registrar un nuevo cliente en el sistema. Retorna HTTP 201 (Created) con los
     * datos del cliente recién creado.
     */
    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteDTO dto) {
        Cliente nuevo = clienteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    /**
     * Endpoint para actualizar los datos de un cliente existente. Retorna HTTP 200 (OK) con los
     * datos actualizados.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(
            @PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {
        Cliente actualizado = clienteService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Endpoint para eliminar un cliente por su ID. Retorna HTTP 204 (No Content) sin cuerpo ante
     * una eliminación exitosa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
