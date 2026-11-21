package cl.duoc.foro.moderacion.service;

import cl.duoc.foro.moderacion.model.Reporte;
import cl.duoc.foro.moderacion.repository.ReporteRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Servicio de Reporte. Hereda el CRUD completo de {@link AbstractCrudService}
 * (Template Method) y solo define los pasos propios de la entidad.
 *
 * Patrón SINGLETON: {@code @Service} registra una única instancia en el
 * contenedor de Spring, compartida por toda la aplicación.
 * Inyección de dependencias: el repositorio y el publicador de eventos llegan
 * por el constructor.
 */
@Service
public class ReporteService extends AbstractCrudService<Reporte, Long> {

    public ReporteService(ReporteRepository repositorio, ApplicationEventPublisher publicadorEventos) {
        super(repositorio, publicadorEventos);
    }

    @Override
    protected String nombreRecurso() {
        return "reporte";
    }

    @Override
    protected void copiarDatos(Reporte origen, Reporte destino) {
        destino.setNombre(origen.getNombre());
    }
}
