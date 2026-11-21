package cl.duoc.foro.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Usuario del foro (tabla FORO_USUARIOS).
 *
 * Patrón BUILDER: {@code Usuario.builder().nombre("...").alias("...").build()}.
 * La contraseña se guarda solo como hash BCrypt.
 */
@Entity
@Table(name = "FORO_USUARIOS")
public class Usuario implements Identificable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ALIAS", nullable = false, unique = true, length = 20)
    private String alias;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 100)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ROL_ID", nullable = false)
    private Rol rol;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = Boolean.TRUE;

    @Column(name = "BIOGRAFIA", length = 300)
    private String biografia;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    /** Código temporal para recuperar la contraseña (en un sistema real se enviaría por correo). */
    @Column(name = "CODIGO_RECUPERACION", length = 6)
    private String codigoRecuperacion;

    @Column(name = "CODIGO_EXPIRA")
    private LocalDateTime codigoExpira;

    protected Usuario() {
    }

    private Usuario(Builder b) {
        this.id = b.id;
        this.nombre = b.nombre;
        this.alias = b.alias;
        this.email = b.email;
        this.passwordHash = b.passwordHash;
        this.rol = b.rol;
        this.activo = b.activo;
        this.biografia = b.biografia;
        this.fechaRegistro = b.fechaRegistro;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getCodigoRecuperacion() {
        return codigoRecuperacion;
    }

    public void setCodigoRecuperacion(String codigoRecuperacion) {
        this.codigoRecuperacion = codigoRecuperacion;
    }

    public LocalDateTime getCodigoExpira() {
        return codigoExpira;
    }

    public void setCodigoExpira(LocalDateTime codigoExpira) {
        this.codigoExpira = codigoExpira;
    }

    public boolean esAdmin() {
        return rol != null && rol.esAdmin();
    }

    /** Patrón Builder: construye un {@link Usuario} paso a paso. */
    public static final class Builder {
        private Long id;
        private String nombre;
        private String alias;
        private String email;
        private String passwordHash;
        private Rol rol;
        private Boolean activo = Boolean.TRUE;
        private String biografia;
        private LocalDateTime fechaRegistro = LocalDateTime.now();

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

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder rol(Rol rol) {
            this.rol = rol;
            return this;
        }

        public Builder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Builder biografia(String biografia) {
            this.biografia = biografia;
            return this;
        }

        public Builder fechaRegistro(LocalDateTime fechaRegistro) {
            this.fechaRegistro = fechaRegistro;
            return this;
        }

        public Usuario build() {
            return new Usuario(this);
        }
    }
}
