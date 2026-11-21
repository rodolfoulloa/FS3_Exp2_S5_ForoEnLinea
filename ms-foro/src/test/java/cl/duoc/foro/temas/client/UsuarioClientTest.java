package cl.duoc.foro.temas.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cl.duoc.foro.temas.exception.AccesoDenegadoException;
import cl.duoc.foro.temas.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Comunicación con ms-usuarios simulada con MockRestServiceServer. */
class UsuarioClientTest {

    private MockRestServiceServer servidor;
    private UsuarioClient cliente;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new UsuarioClient(builder, "http://ms-usuarios");
    }

    private void responder(long id, String json) {
        servidor.expect(requestTo("http://ms-usuarios/usuarios/" + id))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    @Test
    void obtener_leeAliasRolYEstado() {
        responder(3, "{\"id\":3,\"alias\":\"vale.soto\",\"rol\":\"USUARIO\",\"activo\":true,\"email\":\"v@c.cl\"}");

        var u = cliente.obtener(3L);

        assertThat(u.alias()).isEqualTo("vale.soto");
        assertThat(u.esAdmin()).isFalse();
    }

    @Test
    void obtener_inexistente_lanza404() {
        servidor.expect(requestTo("http://ms-usuarios/usuarios/99")).andRespond(withResourceNotFound());

        assertThatThrownBy(() -> cliente.obtener(99L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void exigirAdmin_conUsuarioNormal_lanza403() {
        responder(3, "{\"id\":3,\"alias\":\"vale.soto\",\"rol\":\"USUARIO\",\"activo\":true}");

        assertThatThrownBy(() -> cliente.exigirAdmin(3L))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("Esta operación requiere el rol ADMIN");
    }

    @Test
    void obtenerActivo_conCuentaDesactivada_lanza403() {
        responder(6, "{\"id\":6,\"alias\":\"tomas.dev\",\"rol\":\"USUARIO\",\"activo\":false}");

        assertThatThrownBy(() -> cliente.obtenerActivo(6L)).hasMessage("La cuenta tomas.dev está desactivada");
    }

    @Test
    void exigirAdmin_conAdminActivo_loDevuelve() {
        responder(1, "{\"id\":1,\"alias\":\"camila.admin\",\"rol\":\"ADMIN\",\"activo\":true}");

        assertThat(cliente.exigirAdmin(1L).alias()).isEqualTo("camila.admin");
    }
}
