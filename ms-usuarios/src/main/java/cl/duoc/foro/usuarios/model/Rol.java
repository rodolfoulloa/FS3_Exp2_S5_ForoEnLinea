package cl.duoc.foro.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Rol de los usuarios del foro (tabla FORO_ROLES).
 * <ul>
 *   <li>USUARIO: navega, publica temas, comenta, reporta y edita su perfil.</li>
 *   <li>ADMIN: además administra usuarios y modera el contenido (banear temas y comentarios).</li>
 * </ul>
 */
@Entity
@Table(name = "FORO_ROLES")
public class Rol implements Identificable<Long> {

    public static final String USUARIO = "USUARIO";
    public static final String ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(name = "DESCRIPCION", nullable = false, length = 200)
    private String descripcion;

    protected Rol() {
    }

    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public boolean esAdmin() {
        return ADMIN.equals(nombre);
    }
}
