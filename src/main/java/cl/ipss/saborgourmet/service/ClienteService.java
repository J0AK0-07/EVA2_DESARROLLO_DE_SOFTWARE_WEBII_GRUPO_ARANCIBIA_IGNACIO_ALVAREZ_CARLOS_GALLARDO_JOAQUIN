package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.ClienteDTO;
import cl.ipss.saborgourmet.exception.*;
import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.repository.*;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

/** Gestiona clientes y protege el historial de reservas mediante transacciones JPA. */
@Service
@Transactional(readOnly = true)
public class ClienteService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final ClienteRepository clientes;
    private final ReservaRepository reservas;

    public ClienteService(ClienteRepository clientes, ReservaRepository reservas) {
        this.clientes = clientes;
        this.reservas = reservas;
    }

    public List<Cliente> listarTodos() {
        return clientes.findAll(Sort.by("nombre", "id"));
    }

    public Cliente obtenerPorId(Long id) {
        if (id == null) throw new BadRequestException("Indique el cliente.");
        return clientes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));
    }

    @Transactional
    public Cliente crear(ClienteDTO dto) {
        validar(dto, null);
        return clientes.save(
                new Cliente(
                        null,
                        dto.getNombre().trim(),
                        dto.getEmail().trim().toLowerCase(Locale.ROOT),
                        dto.getTelefono().trim()));
    }

    @Transactional
    public Cliente actualizar(Long id, ClienteDTO dto) {
        Cliente cliente =
                clientes.bloquearPorId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));
        validar(dto, id);
        cliente.setNombre(dto.getNombre().trim());
        cliente.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));
        cliente.setTelefono(dto.getTelefono().trim());
        return clientes.save(cliente);
    }

    /** No se elimina un cliente vinculado, incluso si sus reservas ya fueron canceladas. */
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente =
                clientes.bloquearPorId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));
        if (reservas.existsByCliente_Id(id))
            throw new ConflictException(
                    "El cliente tiene reservas asociadas. Se conserva para proteger el historial.");
        clientes.delete(cliente);
    }

    private void validar(ClienteDTO dto, Long excluir) {
        if (dto == null
                || dto.getNombre() == null
                || dto.getNombre().isBlank()
                || dto.getNombre().trim().length() > 100)
            throw new BadRequestException(
                    "El nombre es obligatorio y admite hasta 100 caracteres.");
        if (dto.getEmail() == null
                || dto.getEmail().trim().length() > 150
                || !EMAIL.matcher(dto.getEmail().trim()).matches())
            throw new BadRequestException(
                    "Ingrese un correo electrónico válido, de hasta 150 caracteres.");
        if (dto.getTelefono() == null
                || dto.getTelefono().isBlank()
                || dto.getTelefono().trim().length() > 30)
            throw new BadRequestException(
                    "El teléfono es obligatorio y admite hasta 30 caracteres.");
        clientes.findByEmailIgnoreCase(dto.getEmail().trim())
                .ifPresent(
                        c -> {
                            if (!c.getId().equals(excluir))
                                throw new ConflictException(
                                        "Ya existe un cliente con ese correo electrónico.");
                        });
    }
}
