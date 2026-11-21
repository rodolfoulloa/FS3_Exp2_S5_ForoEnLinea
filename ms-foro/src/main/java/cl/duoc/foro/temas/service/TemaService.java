package cl.duoc.foro.temas.service;

import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.repository.TemaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Servicio de Tema. Hereda el CRUD completo de {@link AbstractCrudService}
 * (Template Method) y solo define los pasos propios de la entidad.
 *
 * Patrón SINGLETON: {@code @Service} registra una única instancia en el
 * contenedor de Spring, compartida por toda la aplicación.
 * Inyección de dependencias: el repositorio y el publicador de eventos llegan
 * por el constructor.
 */
@Service
public class TemaService extends AbstractCrudService<Tema, Long> {

    public TemaService(TemaRepository repositorio, ApplicationEventPublisher publicadorEventos) {
        super(repositorio, publicadorEventos);
    }

    @Override
    protected String nombreRecurso() {
        return "tema";
    }

    @Override
    protected void copiarDatos(Tema origen, Tema destino) {
        destino.setNombre(origen.getNombre());
    }
}
