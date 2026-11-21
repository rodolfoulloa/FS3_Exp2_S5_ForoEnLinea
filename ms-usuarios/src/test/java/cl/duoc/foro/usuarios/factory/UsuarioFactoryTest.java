package cl.duoc.foro.usuarios.factory;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.foro.usuarios.dto.RegistroRequest;
import cl.duoc.foro.usuarios.dto.UsuarioRequest;
import cl.duoc.foro.usuarios.dto.UsuarioResponse;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import org.junit.jupiter.api.Test;

/** Patrón Factory (y Builder): normalización de datos y respuestas sin contraseña. */
class UsuarioFactoryTest {

    private final UsuarioFactory factory = new UsuarioFactory();
    private final Rol rolUsuario = new Rol(Rol.USUARIO, "Usuario");

    @Test
    void crearDesdeRegistro_normalizaLosDatosYAsignaElRol() {
        RegistroRequest req = new RegistroRequest("  Ana Prueba ", " ana.prueba ", " Ana@Correo.CL ",
                "Prueba#2025", "Prueba#2025", true);

        Usuario u = factory.crearDesdeRegistro(req, rolUsuario, "hash");

        assertThat(u.getId()).isNull();
        assertThat(u.getNombre()).isEqualTo("Ana Prueba");
        assertThat(u.getAlias()).isEqualTo("ana.prueba");
        assertThat(u.getEmail()).isEqualTo("ana@correo.cl");
        assertThat(u.getPasswordHash()).isEqualTo("hash");
        assertThat(u.getRol()).isSameAs(rolUsuario);
        assertThat(u.getActivo()).isTrue();
        assertThat(u.getFechaRegistro()).isNotNull();
    }

    @Test
    void crearDatosPerfil_dejaLaBiografiaVaciaComoNula() {
        Usuario datos = factory.crearDatosPerfil(new UsuarioRequest("Vale", "vale", "V@C.CL", "   "));

        assertThat(datos.getBiografia()).isNull();
        assertThat(datos.getEmail()).isEqualTo("v@c.cl");
    }

    @Test
    void crearRespuesta_incluyeElRolYNuncaLaContrasena() {
        Usuario u = Usuario.builder().id(3L).nombre("Vale").alias("vale").email("v@c.cl")
                .passwordHash("secreto").rol(rolUsuario).build();

        UsuarioResponse r = factory.crearRespuesta(u);

        assertThat(r.rol()).isEqualTo("USUARIO");
        assertThat(r.toString()).doesNotContain("secreto");
    }
}
