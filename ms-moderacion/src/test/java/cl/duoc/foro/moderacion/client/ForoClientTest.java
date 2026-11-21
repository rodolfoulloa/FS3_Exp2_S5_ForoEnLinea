package cl.duoc.foro.moderacion.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cl.duoc.foro.moderacion.exception.AccesoDenegadoException;
import cl.duoc.foro.moderacion.exception.RecursoNoEncontradoException;
import cl.duoc.foro.moderacion.strategy.ComentarioModeracionStrategy;
import cl.duoc.foro.moderacion.strategy.TemaModeracionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Comunicación con ms-foro (y las estrategias que la usan) con un servidor simulado. */
class ForoClientTest {

    private MockRestServiceServer servidor;
    private ForoClient cliente;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new ForoClient(builder, "http://ms-foro");
    }

    @Test
    void estrategiaDeTemas_consultaElEstadoSinSumarVisitas() {
        servidor.expect(requestTo("http://ms-foro/temas/11/estado"))
                .andRespond(withSuccess("{\"id\":11,\"titulo\":\"GANA DINERO\",\"autorId\":6,\"estado\":\"BANEADO\",\"vistas\":12}",
                        MediaType.APPLICATION_JSON));

        var tema = new TemaModeracionStrategy(cliente).obtener(11L);

        assertThat(tema.estaBaneado()).isTrue();
        assertThat(tema.resumen()).isEqualTo("GANA DINERO");
    }

    @Test
    void estrategiaDeComentarios_enviaElPatchConElAdminYElEstado() {
        servidor.expect(requestTo("http://ms-foro/comentarios/16/moderacion"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Usuario-Id", "1"))
                .andExpect(content().json("{\"estado\":\"BANEADO\",\"motivo\":\"Lenguaje ofensivo\"}"))
                .andRespond(withSuccess("{\"id\":16,\"contenido\":\"Insultos\",\"estado\":\"BANEADO\"}", MediaType.APPLICATION_JSON));

        var c = new ComentarioModeracionStrategy(cliente).aplicarEstado(16L, "BANEADO", "Lenguaje ofensivo", 1L);

        assertThat(c.estado()).isEqualTo("BANEADO");
        servidor.verify();
    }

    @Test
    void contenidoInexistente_lanza404() {
        servidor.expect(requestTo("http://ms-foro/temas/999/estado")).andRespond(withResourceNotFound());

        assertThatThrownBy(() -> cliente.obtenerTema(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe un tema con ID 999");
    }

    @Test
    void msForoRechazaPorRol_lanza403() {
        servidor.expect(requestTo("http://ms-foro/temas/11/moderacion")).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> cliente.moderarTema(11L, "BANEADO", "Spam", 3L))
                .isInstanceOf(AccesoDenegadoException.class);
    }
}
