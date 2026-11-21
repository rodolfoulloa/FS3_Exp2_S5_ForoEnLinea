package cl.duoc.foro.moderacion.client;

import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.exception.AccesoDenegadoException;
import cl.duoc.foro.moderacion.exception.RecursoNoEncontradoException;
import cl.duoc.foro.moderacion.exception.ServicioNoDisponibleException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente REST hacia ms-foro (patrón PROXY / Gateway). ms-moderacion no toca
 * las tablas de temas ni comentarios: le pide a ms-foro que cambie su estado.
 */
@Component
public class ForoClient {

    private static final Logger log = LoggerFactory.getLogger(ForoClient.class);
    private static final String HEADER = "X-Usuario-Id";

    private final RestClient restClient;

    public ForoClient(RestClient.Builder builder, @Value("${foro.service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /** Tema con su estado, sin sumar visitas (GET /temas/{id}/estado). */
    public ContenidoRemoto obtenerTema(Long id) {
        return ejecutar("tema", id, () -> restClient.get().uri("/temas/{id}/estado", id)
                .retrieve().body(ContenidoRemoto.class));
    }

    /** Comentario con su estado (GET /comentarios/{id}). */
    public ContenidoRemoto obtenerComentario(Long id) {
        return ejecutar("comentario", id, () -> restClient.get().uri("/comentarios/{id}", id)
                .retrieve().body(ContenidoRemoto.class));
    }

    /** PATCH /temas/{id}/moderacion en nombre del ADMIN. */
    public ContenidoRemoto moderarTema(Long id, String estado, String motivo, Long adminId) {
        return ejecutar("tema", id, () -> restClient.patch().uri("/temas/{id}/moderacion", id)
                .header(HEADER, String.valueOf(adminId)).contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo(estado, motivo)).retrieve().body(ContenidoRemoto.class));
    }

    /** PATCH /comentarios/{id}/moderacion en nombre del ADMIN. */
    public ContenidoRemoto moderarComentario(Long id, String estado, String motivo, Long adminId) {
        return ejecutar("comentario", id, () -> restClient.patch().uri("/comentarios/{id}/moderacion", id)
                .header(HEADER, String.valueOf(adminId)).contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo(estado, motivo)).retrieve().body(ContenidoRemoto.class));
    }

    private Map<String, String> cuerpo(String estado, String motivo) {
        Map<String, String> body = new HashMap<>();
        body.put("estado", estado);
        body.put("motivo", motivo);
        return body;
    }

    private ContenidoRemoto ejecutar(String recurso, Long id, java.util.function.Supplier<ContenidoRemoto> llamada) {
        try {
            return llamada.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException(recurso, id);
        } catch (HttpClientErrorException.Forbidden e) {
            throw new AccesoDenegadoException("ms-foro rechazó la operación: se requiere el rol ADMIN");
        } catch (ResourceAccessException e) {
            log.error("ms-foro no responde: {}", e.getMessage());
            throw new ServicioNoDisponibleException("El microservicio del foro no está disponible", e);
        } catch (RestClientException e) {
            log.error("Error al comunicarse con ms-foro: {}", e.getMessage());
            throw new ServicioNoDisponibleException("Error al comunicarse con el microservicio del foro", e);
        }
    }
}
