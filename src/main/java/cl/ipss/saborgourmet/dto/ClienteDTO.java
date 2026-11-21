package cl.ipss.saborgourmet.dto;

import jakarta.validation.constraints.*;

/** DTO para la transferencia de información de Clientes. */
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "Ingrese el nombre.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingrese el correo.")
    @Email(message = "Ingrese un correo válido.")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Ingrese el teléfono.")
    @Size(max = 30)
    private String telefono;

    public ClienteDTO(String nombre, String email, String telefono) {
        this(null, nombre, email, telefono);
    }

    public ClienteDTO() {}

    public ClienteDTO(Long id, String nombre, String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
