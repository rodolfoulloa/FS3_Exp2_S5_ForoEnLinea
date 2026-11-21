package cl.duoc.foro.temas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** DTOs de entrada y salida de categorías, temas, comentarios y moderación. */
public final class ForoDtos {

    private ForoDtos() {
    }

    /** POST/PUT /temas: publicar o editar un tema. */
    public record TemaRequest(
            @NotBlank(message = "El título es obligatorio")
            @Size(min = 10, max = 150, message = "El título debe tener entre 10 y 150 caracteres")
            String titulo,

            @NotBlank(message = "El contenido es obligatorio")
            @Size(min = 20, max = 2000, message = "El contenido debe tener entre 20 y 2000 caracteres")
            String contenido,

            @NotNull(message = "La categoría es obligatoria")
            @Positive(message = "La categoría no es válida")
            Long categoriaId) {
    }

    public record TemaResponse(
            Long id,
            String titulo,
            String contenido,
            Long categoriaId,
            String categoria,
            Long autorId,
            String autorAlias,
            String estado,
            String motivoBaneo,
            Integer vistas,
            long comentarios,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion) {
    }

    /** POST/PUT de comentarios. */
    public record ComentarioRequest(
            @NotBlank(message = "El comentario no puede estar vacío")
            @Size(min = 2, max = 1000, message = "El comentario debe tener entre 2 y 1000 caracteres")
            String contenido) {
    }

    public record ComentarioResponse(
            Long id,
            Long temaId,
            Long autorId,
            String autorAlias,
            String contenido,
            String estado,
            String motivoBaneo,
            LocalDateTime fecha) {
    }

    public record CategoriaResponse(Long id, String nombre, String descripcion, String icono, String color, long temas) {
    }

    /** PATCH .../moderacion: lo usa ms-moderacion para banear o restaurar contenido. */
    public record ModeracionRequest(
            @NotBlank(message = "El estado es obligatorio")
            @Pattern(regexp = "^(ACTIVO|BANEADO)$", message = "El estado debe ser ACTIVO o BANEADO")
            String estado,

            @Size(max = 300, message = "El motivo admite hasta 300 caracteres")
            String motivo) {
    }
}
