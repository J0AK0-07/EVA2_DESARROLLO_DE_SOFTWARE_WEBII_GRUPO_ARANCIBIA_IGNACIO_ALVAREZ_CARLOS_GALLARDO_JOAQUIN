package cl.ipss.saborgourmet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia de datos (DTO) para estandarizar las respuestas de error de la API REST.
 * Proporciona información clara y estructurada al cliente sin exponer trazas internas de la
 * aplicación.
 */
public class ErrorResponseDTO {

    /** Marca de tiempo exacta en que se generó el error */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /** Código de estado HTTP numérico (ej. 400, 404, 409, 500) */
    private int status;

    /** Nombre descriptivo del error HTTP (ej. "Not Found", "Conflict", "Bad Request") */
    private String error;

    /** Mensaje claro y específico con la causa del error */
    private String message;

    /** Ruta del endpoint URI en el cual ocurrió la excepción */
    private String path;

    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters y Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
