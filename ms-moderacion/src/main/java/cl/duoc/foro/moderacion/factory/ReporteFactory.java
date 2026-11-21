package cl.duoc.foro.moderacion.factory;

import cl.duoc.foro.moderacion.dto.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ReporteResponse;
import cl.duoc.foro.moderacion.model.Reporte;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Patrón FACTORY: centraliza la creación de objetos {@link Reporte} y
 * {@link ReporteResponse}. El controlador no usa {@code new} ni conoce cómo se
 * arma cada objeto; si cambia la forma de construirlos, solo cambia esta clase.
 */
@Component
public class ReporteFactory {

    /** Crea la entidad a partir de los datos recibidos (usa el patrón Builder). */
    public Reporte crearEntidad(ReporteRequest request) {
        return Reporte.builder()
                .nombre(request.nombre().trim())
                .build();
    }

    /** Crea la respuesta JSON a partir de la entidad. */
    public ReporteResponse crearRespuesta(Reporte entidad) {
        return new ReporteResponse(entidad.getId(), entidad.getNombre());
    }

    /** Crea la lista de respuestas a partir de una lista de entidades. */
    public List<ReporteResponse> crearRespuestas(List<Reporte> entidades) {
        return entidades.stream().map(this::crearRespuesta).toList();
    }
}
