package cl.duoc.foro.temas.client;

import cl.duoc.foro.temas.dto.UsuarioRemoto;
import cl.duoc.foro.temas.exception.AccesoDenegadoException;
import cl.duoc.foro.temas.exception.RecursoNoEncontradoException;
import cl.duoc.foro.temas.exception.ServicioNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente REST hacia ms-usuarios (patrón PROXY / Gateway): el resto del
 * microservicio pide "el usuario X" sin saber que vive en otro servicio.
 * Cada microservicio es dueño de sus tablas, por eso ms-foro no lee FORO_USUARIOS.
 */
@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);

    private final RestClient restClient;

    public UsuarioClient(RestClient.Builder builder, @Value("${usuarios.service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /** Obtiene el usuario (alias, rol y estado). 404 si no existe, 503 si ms-usuarios no responde. */
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

    /** Obtiene el usuario y exige que su cuenta esté activa. */
    public UsuarioRemoto obtenerActivo(Long id) {
        UsuarioRemoto u = obtener(id);
        if (!Boolean.TRUE.equals(u.activo())) {
            throw new AccesoDenegadoException("La cuenta " + u.alias() + " está desactivada");
        }
        return u;
    }

    /** Obtiene el usuario y exige que sea un ADMIN activo. */
    public UsuarioRemoto exigirAdmin(Long id) {
        UsuarioRemoto u = obtenerActivo(id);
        if (!u.esAdmin()) {
            throw new AccesoDenegadoException("Esta operación requiere el rol ADMIN");
        }
        return u;
    }
}
