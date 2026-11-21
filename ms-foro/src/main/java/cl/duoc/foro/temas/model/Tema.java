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

/**
 * Tema publicado en el foro (tabla FORO_TEMAS).
 *
 * El autor pertenece a ms-usuarios: aquí se guarda su ID y su alias (copiado
 * al publicar), no una relación a la tabla de usuarios. Patrón BUILDER.
 */
@Entity
@Table(name = "FORO_TEMAS")
public class Tema implements Identificable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITULO", nullable = false, length = 150)
    private String titulo;

    @Column(name = "CONTENIDO", nullable = false, length = 2000)
    private String contenido;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "CATEGORIA_ID", nullable = false)
    private Categoria categoria;

    @Column(name = "AUTOR_ID", nullable = false)
    private Long autorId;

    @Column(name = "AUTOR_ALIAS", nullable = false, length = 20)
    private String autorAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 10)
    private EstadoContenido estado = EstadoContenido.ACTIVO;

    @Column(name = "MOTIVO_BANEO", length = 300)
    private String motivoBaneo;

    @Column(name = "VISTAS", nullable = false)
    private Integer vistas = 0;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    protected Tema() {
    }

    private Tema(Builder b) {
        this.id = b.id;
        this.titulo = b.titulo;
        this.contenido = b.contenido;
        this.categoria = b.categoria;
        this.autorId = b.autorId;
        this.autorAlias = b.autorAlias;
        this.fechaCreacion = b.fechaCreacion;
        this.vistas = b.vistas;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getAutorAlias() {
        return autorAlias;
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

    public Integer getVistas() {
        return vistas;
    }

    public void sumarVista() {
        this.vistas = (vistas == null ? 0 : vistas) + 1;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public boolean estaBaneado() {
        return estado == EstadoContenido.BANEADO;
    }

    /** Patrón Builder: construye un {@link Tema} paso a paso. */
    public static final class Builder {
        private Long id;
        private String titulo;
        private String contenido;
        private Categoria categoria;
        private Long autorId;
        private String autorAlias;
        private Integer vistas = 0;
        private LocalDateTime fechaCreacion = LocalDateTime.now();

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder titulo(String titulo) {
            this.titulo = titulo;
            return this;
        }

        public Builder contenido(String contenido) {
            this.contenido = contenido;
            return this;
        }

        public Builder categoria(Categoria categoria) {
            this.categoria = categoria;
            return this;
        }

        public Builder autor(Long autorId, String autorAlias) {
            this.autorId = autorId;
            this.autorAlias = autorAlias;
            return this;
        }

        public Builder vistas(Integer vistas) {
            this.vistas = vistas;
            return this;
        }

        public Builder fechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public Tema build() {
            return new Tema(this);
        }
    }
}
