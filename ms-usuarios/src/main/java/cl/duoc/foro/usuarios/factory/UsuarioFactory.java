package cl.duoc.foro.usuarios.factory;

import cl.duoc.foro.usuarios.dto.UsuarioRequest;
import cl.duoc.foro.usuarios.dto.UsuarioResponse;
import cl.duoc.foro.usuarios.model.Usuario;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Patrón FACTORY: centraliza la creación de objetos {@link Usuario} y
 * {@link UsuarioResponse}. El controlador no usa {@code new} ni conoce cómo se
 * arma cada objeto; si cambia la forma de construirlos, solo cambia esta clase.
 */
@Component
public class UsuarioFactory {

    /** Crea la entidad a partir de los datos recibidos (usa el patrón Builder). */
    public Usuario crearEntidad(UsuarioRequest request) {
        return Usuario.builder()
                .nombre(request.nombre().trim())
                .build();
    }

    /** Crea la respuesta JSON a partir de la entidad. */
    public UsuarioResponse crearRespuesta(Usuario entidad) {
        return new UsuarioResponse(entidad.getId(), entidad.getNombre());
    }

    /** Crea la lista de respuestas a partir de una lista de entidades. */
    public List<UsuarioResponse> crearRespuestas(List<Usuario> entidades) {
        return entidades.stream().map(this::crearRespuesta).toList();
    }
}
