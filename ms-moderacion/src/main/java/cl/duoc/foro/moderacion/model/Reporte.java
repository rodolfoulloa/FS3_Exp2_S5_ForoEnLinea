package cl.duoc.foro.moderacion.model;

import cl.duoc.foro.moderacion.model.Enums.EstadoReporte;
import cl.duoc.foro.moderacion.model.Enums.MotivoReporte;
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
 * Reporte de un usuario sobre un tema o comentario que incumple las normas
 * (tabla FORO_REPORTES). Queda PENDIENTE hasta que un ADMIN lo resuelva
 * (baneando el contenido) o lo descarte. Patrón BUILDER.
 */
@Entity
@Table(name = "FORO_REPORTES")
public class Reporte implements Identificable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONTENIDO", nullable = false, length = 12)
    private TipoContenido tipoContenido;

    @Column(name = "CONTENIDO_ID", nullable = false)
    private Long contenidoId;

    /** Título del tema o extracto del comentario al momento del reporte. */
    @Column(name = "RESUMEN_CONTENIDO", nullable = false, length = 160)
    private String resumenContenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "MOTIVO", nullable = false, length = 25)
    private MotivoReporte motivo;

    @Column(name = "DETALLE", length = 300)
    private String detalle;

    @Column(name = "REPORTANTE_ID", nullable = false)
    private Long reportanteId;

    @Column(name = "REPORTANTE_ALIAS", nullable = false, length = 20)
    private String reportanteAlias;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 12)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Column(name = "RESUELTO_POR_ALIAS", length = 20)
    private String resueltoPorAlias;

    @Column(name = "RESOLUCION", length = 300)
    private String resolucion;

    @Column(name = "FECHA_RESOLUCION")
    private LocalDateTime fechaResolucion;

    protected Reporte() {
    }

    private Reporte(Builder b) {
        this.id = b.id;
        this.tipoContenido = b.tipoContenido;
        this.contenidoId = b.contenidoId;
        this.resumenContenido = b.resumenContenido;
        this.motivo = b.motivo;
        this.detalle = b.detalle;
        this.reportanteId = b.reportanteId;
        this.reportanteAlias = b.reportanteAlias;
        this.fecha = b.fecha;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Cierra el reporte con el estado indicado (RESUELTO o DESCARTADO). */
    public void cerrar(EstadoReporte nuevoEstado, String adminAlias, String resolucion) {
        this.estado = nuevoEstado;
        this.resueltoPorAlias = adminAlias;
        this.resolucion = resolucion;
        this.fechaResolucion = LocalDateTime.now();
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

    public MotivoReporte getMotivo() {
        return motivo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Long getReportanteId() {
        return reportanteId;
    }

    public String getReportanteAlias() {
        return reportanteAlias;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public String getResueltoPorAlias() {
        return resueltoPorAlias;
    }

    public String getResolucion() {
        return resolucion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public boolean estaPendiente() {
        return estado == EstadoReporte.PENDIENTE;
    }

    /** Patrón Builder: construye un {@link Reporte} paso a paso. */
    public static final class Builder {
        private Long id;
        private TipoContenido tipoContenido;
        private Long contenidoId;
        private String resumenContenido;
        private MotivoReporte motivo;
        private String detalle;
        private Long reportanteId;
        private String reportanteAlias;
        private LocalDateTime fecha = LocalDateTime.now();

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder contenido(TipoContenido tipo, Long contenidoId, String resumen) {
            this.tipoContenido = tipo;
            this.contenidoId = contenidoId;
            this.resumenContenido = resumen;
            return this;
        }

        public Builder motivo(MotivoReporte motivo, String detalle) {
            this.motivo = motivo;
            this.detalle = detalle;
            return this;
        }

        public Builder reportante(Long id, String alias) {
            this.reportanteId = id;
            this.reportanteAlias = alias;
            return this;
        }

        public Builder fecha(LocalDateTime fecha) {
            this.fecha = fecha;
            return this;
        }

        public Reporte build() {
            return new Reporte(this);
        }
    }
}
