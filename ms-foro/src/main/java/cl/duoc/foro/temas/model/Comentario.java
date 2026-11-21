package cl.duoc.foro.temas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Comentario de un tema (tabla FORO_COMENTARIOS). Patrón BUILDER. */
@Entity
@Table(name = "FORO_COMENTARIOS")
public class Comentario implements Identificable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TEMA_ID", nullable = false)
    private Tema tema;

    @Column(name = "AUTOR_ID", nullable = false)
    private Long autorId;

    @Column(name = "AUTOR_ALIAS", nullable = false, length = 20)
    private String autorAlias;

    @Column(name = "CONTENIDO", nullable = false, length = 1000)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 10)
    private EstadoContenido estado = EstadoContenido.ACTIVO;

    @Column(name = "MOTIVO_BANEO", length = 300)
    private String motivoBaneo;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    protected Comentario() {
    }

    private Comentario(Builder b) {
        this.id = b.id;
        this.tema = b.tema;
        this.autorId = b.autorId;
        this.autorAlias = b.autorAlias;
        this.contenido = b.contenido;
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

    public Tema getTema() {
        return tema;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getAutorAlias() {
        return autorAlias;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public EstadoContenido getEstado() {
        return estado;
    }

    public void setEstado(EstadoContenido estado) {
        this.estado = estado;
    }

    public String getMotivoBaneo() {
        return motivoBaneo;
    }

    public void setMotivoBaneo(String motivoBaneo) {
        this.motivoBaneo = motivoBaneo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean estaBaneado() {
        return estado == EstadoContenido.BANEADO;
    }

    /** Patrón Builder: construye un {@link Comentario} paso a paso. */
    public static final class Builder {
        private Long id;
        private Tema tema;
        private Long autorId;
        private String autorAlias;
        private String contenido;
        private LocalDateTime fecha = LocalDateTime.now();

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder tema(Tema tema) {
            this.tema = tema;
            return this;
        }

        public Builder autor(Long autorId, String autorAlias) {
            this.autorId = autorId;
            this.autorAlias = autorAlias;
            return this;
        }

        public Builder contenido(String contenido) {
            this.contenido = contenido;
            return this;
        }

        public Builder fecha(LocalDateTime fecha) {
            this.fecha = fecha;
            return this;
        }

        public Comentario build() {
            return new Comentario(this);
        }
    }
}
