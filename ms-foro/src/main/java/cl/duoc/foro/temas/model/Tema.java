package cl.duoc.foro.temas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA del microservicio (tabla FORO_TEMAS).
 *
 * Patrón BUILDER: la clase interna {@link Builder} permite construir la entidad
 * paso a paso y de forma legible: {@code Tema.builder().nombre("...").build()}.
 * Agrega aquí los atributos propios de tu entidad (y en su Builder).
 */
@Entity
@Table(name = "FORO_TEMAS")
public class Tema implements Identificable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 200)
    private String nombre;

    /** Constructor vacío requerido por JPA. */
    protected Tema() {
    }

    private Tema(Builder builder) {
        this.id = builder.id;
        this.nombre = builder.nombre;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Patrón Builder: construye un {@link Tema} paso a paso. */
    public static final class Builder {
        private Long id;
        private String nombre;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Tema build() {
            return new Tema(this);
        }
    }
}
