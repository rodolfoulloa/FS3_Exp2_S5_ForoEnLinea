package cl.duoc.foro.temas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Categoría de temas del foro (tabla FORO_CATEGORIAS). El foro tiene 6 categorías. */
@Entity
@Table(name = "FORO_CATEGORIAS")
public class Categoria implements Identificable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "DESCRIPCION", nullable = false, length = 200)
    private String descripcion;

    /** Nombre del ícono de Bootstrap Icons que usa el frontend (ej: "cpu"). */
    @Column(name = "ICONO", nullable = false, length = 30)
    private String icono;

    /** Color de la categoría en formato hexadecimal (ej: "#2F6F5E"). */
    @Column(name = "COLOR", nullable = false, length = 7)
    private String color;

    protected Categoria() {
    }

    public Categoria(String nombre, String descripcion, String icono, String color) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.color = color;
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

    public String getDescripcion() {
        return descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public String getColor() {
        return color;
    }
}
