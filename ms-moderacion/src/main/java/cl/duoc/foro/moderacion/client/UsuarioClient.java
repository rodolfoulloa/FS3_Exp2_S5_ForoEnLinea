package cl.duoc.foro.moderacion.client;

import cl.duoc.foro.moderacion.dto.ModeracionDtos.UsuarioRemoto;
import cl.duoc.foro.moderacion.exception.AccesoDenegadoException;
import cl.duoc.foro.moderacion.exception.RecursoNoEncontradoException;
import cl.duoc.foro.moderacion.exception.ServicioNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente REST hacia ms-usuarios (patrón PROXY / Gateway). Se usa para
 * identificar a quien reporta y para exigir el rol ADMIN al moderar.
 */
@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);

    private final RestClient restClient;

    public UsuarioClient(RestClient.Builder builder, @Value("${usuarios.service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public UsuarioRemoto obtener(Long id) {
        try {
            return restClient.get().uri("/usuarios/{id}", id).retrieve().body(UsuarioRemoto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("usuario", id);
        } catch (ResourceAccessException e) {
            log.error("ms-usuarios no responde: {}", e.getMessage());
            throw new ServicioNoDisponibleException("El microservicio de usuarios no está disponible", e);
        } catch (RestClientException e) {
            log.error("Error al consultar ms-usuarios: {}", e.getMessage());
            throw new ServicioNoDisponibleException("Error al comunicarse con el microservicio de usuarios", e);
        }
    }

    /** El usuario debe existir y tener su cuenta activa. */
    public UsuarioRemoto obtenerActivo(Long id) {
        UsuarioRemoto u = obtener(id);
        if (!Boolean.TRUE.equals(u.activo())) {
            throw new AccesoDenegadoException("La cuenta " + u.alias() + " está desactivada");
        }
        return u;
    }

    /** El usuario debe ser un ADMIN activo. */
    public UsuarioRemoto exigirAdmin(Long id) {
        UsuarioRemoto u = obtenerActivo(id);
        if (!u.esAdmin()) {
            throw new AccesoDenegadoException("Esta operación requiere el rol ADMIN");
        }
        return u;
    }
}
