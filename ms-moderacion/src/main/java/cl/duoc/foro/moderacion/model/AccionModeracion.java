package cl.duoc.foro.moderacion.model;

import cl.duoc.foro.moderacion.model.Enums.TipoAccion;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Historial de acciones de moderación (tabla FORO_ACCIONES_MODERACION): qué
 * ADMIN baneó o restauró qué contenido, cuándo y por qué. Patrón BUILDER.
 */
@Entity
@Table(name = "FORO_ACCIONES_MODERACION")
public class AccionModeracion implements Identificable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONTENIDO", nullable = false, length = 12)
    private TipoContenido tipoContenido;

    @Column(name = "CONTENIDO_ID", nullable = false)
    private Long contenidoId;

    @Column(name = "RESUMEN_CONTENIDO", nullable = false, length = 160)
    private String resumenContenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACCION", nullable = false, length = 10)
    private TipoAccion accion;

    @Column(name = "MOTIVO", nullable = false, length = 300)
    private String motivo;

    @Column(name = "ADMIN_ID", nullable = false)
    private Long adminId;

    @Column(name = "ADMIN_ALIAS", nullable = false, length = 20)
    private String adminAlias;

    @Column(name = "REPORTES_RESUELTOS", nullable = false)
    private Integer reportesResueltos = 0;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    protected AccionModeracion() {
    }

    private AccionModeracion(Builder b) {
        this.tipoContenido = b.tipoContenido;
        this.contenidoId = b.contenidoId;
        this.resumenContenido = b.resumenContenido;
        this.accion = b.accion;
        this.motivo = b.motivo;
        this.adminId = b.adminId;
        this.adminAlias = b.adminAlias;
        this.reportesResueltos = b.reportesResueltos;
        this.fecha = b.fecha;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public TipoContenido getTipoContenido() {
        return tipoContenido;
    }

    public Long getContenidoId() {
        return contenidoId;
    }

    public String getResumenContenido() {
        return resumenContenido;
    }

    public TipoAccion getAccion() {
        return accion;
    }

    public String getMotivo() {
        return motivo;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getAdminAlias() {
        return adminAlias;
    }

    public Integer getReportesResueltos() {
        return reportesResueltos;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    /** Patrón Builder: construye una {@link AccionModeracion} paso a paso. */
    public static final class Builder {
        private TipoContenido tipoContenido;
        private Long contenidoId;
        private String resumenContenido;
        private TipoAccion accion;
        private String motivo;
        private Long adminId;
        private String adminAlias;
        private Integer reportesResueltos = 0;
        private LocalDateTime fecha = LocalDateTime.now();

        private Builder() {
        }

        public Builder contenido(TipoContenido tipo, Long contenidoId, String resumen) {
            this.tipoContenido = tipo;
            this.contenidoId = contenidoId;
            this.resumenContenido = resumen;
            return this;
        }

        public Builder accion(TipoAccion accion, String motivo) {
            this.accion = accion;
            this.motivo = motivo;
            return this;
        }

        public Builder admin(Long adminId, String adminAlias) {
            this.adminId = adminId;
            this.adminAlias = adminAlias;
            return this;
        }

        public Builder reportesResueltos(int cantidad) {
            this.reportesResueltos = cantidad;
            return this;
        }

        public Builder fecha(LocalDateTime fecha) {
            this.fecha = fecha;
            return this;
        }

        public AccionModeracion build() {
            return new AccionModeracion(this);
        }
    }
}
