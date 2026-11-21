package cl.duoc.foro.temas.factory;

import cl.duoc.foro.temas.dto.TemaRequest;
import cl.duoc.foro.temas.dto.TemaResponse;
import cl.duoc.foro.temas.model.Tema;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Patrón FACTORY: centraliza la creación de objetos {@link Tema} y
 * {@link TemaResponse}. El controlador no usa {@code new} ni conoce cómo se
 * arma cada objeto; si cambia la forma de construirlos, solo cambia esta clase.
 */
@Component
public class TemaFactory {

    /** Crea la entidad a partir de los datos recibidos (usa el patrón Builder). */
    public Tema crearEntidad(TemaRequest request) {
        return Tema.builder()
                .nombre(request.nombre().trim())
                .build();
    }

    /** Crea la respuesta JSON a partir de la entidad. */
    public TemaResponse crearRespuesta(Tema entidad) {
        return new TemaResponse(entidad.getId(), entidad.getNombre());
    }

    /** Crea la lista de respuestas a partir de una lista de entidades. */
    public List<TemaResponse> crearRespuestas(List<Tema> entidades) {
        return entidades.stream().map(this::crearRespuesta).toList();
    }
}
