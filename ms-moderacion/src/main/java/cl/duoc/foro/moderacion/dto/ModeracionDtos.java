package cl.duoc.foro.moderacion.dto;

import cl.duoc.foro.moderacion.model.Enums.MotivoReporte;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** DTOs de reportes, acciones de moderación y datos remotos. */
public final class ModeracionDtos {

    private ModeracionDtos() {
    }

    /** POST /reportes: un usuario reporta un tema o comentario. */
    public record ReporteRequest(
            @NotNull(message = "Indique si reporta un TEMA o un COMENTARIO")
            TipoContenido tipoContenido,

            @NotNull(message = "El ID del contenido es obligatorio")
            @Positive(message = "El ID del contenido no es válido")
            Long contenidoId,

            @NotNull(message = "El motivo es obligatorio")
            MotivoReporte motivo,

            @Size(max = 300, message = "El detalle admite hasta 300 caracteres")
            String detalle) {

        /** Si el motivo es OTRO, el detalle es obligatorio. */
        @AssertTrue(message = "Describa el motivo en el detalle (mínimo 10 caracteres) cuando elige OTRO")
        public boolean isDetalleSuficiente() {
            return motivo != MotivoReporte.OTRO || (detalle != null && detalle.trim().length() >= 10);
        }
    }

    public record ReporteResponse(
            Long id,
            String tipoContenido,
            Long contenidoId,
            String resumenContenido,
            String motivo,
            String detalle,
            Long reportanteId,
            String reportanteAlias,
            LocalDateTime fecha,
            String estado,
            String resueltoPorAlias,
            String resolucion,
            LocalDateTime fechaResolucion) {
    }

    /** Body de banear, restaurar y descartar. */
    public record DecisionRequest(
            @NotBlank(message = "El motivo es obligatorio")
            @Size(min = 10, max = 300, message = "El motivo debe tener entre 10 y 300 caracteres")
            String motivo) {
    }

    public record AccionResponse(
            Long id,
            String tipoContenido,
            Long contenidoId,
            String resumenContenido,
            String accion,
            String motivo,
            Long adminId,
            String adminAlias,
            Integer reportesResueltos,
            LocalDateTime fecha) {
    }

    /** Resumen para el panel del administrador. */
    public record ResumenResponse(
            long reportesPendientes,
            long reportesResueltos,
            long reportesDescartados,
            long temasBaneados,
            long comentariosBaneados,
            long restauraciones) {
    }

    /** Usuario según ms-usuarios. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsuarioRemoto(Long id, String alias, String rol, Boolean activo) {

        public boolean esAdmin() {
            return "ADMIN".equals(rol);
        }
    }

    /** Tema o comentario según ms-foro (campos comunes a ambos). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContenidoRemoto(Long id, String titulo, String contenido, Long autorId, String autorAlias,
                                  String estado, String motivoBaneo) {

        public boolean estaBaneado() {
            return "BANEADO".equals(estado);
        }

        /** Título del tema o extracto del comentario (máx. 160 caracteres). */
        public String resumen() {
            String texto = titulo != null ? titulo : contenido;
            return texto == null ? "" : texto.length() <= 160 ? texto : texto.substring(0, 157) + "...";
        }
    }
}
