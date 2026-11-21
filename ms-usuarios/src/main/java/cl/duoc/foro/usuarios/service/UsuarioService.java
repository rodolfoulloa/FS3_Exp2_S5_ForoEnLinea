package cl.duoc.foro.usuarios.service;

import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Servicio de Usuario. Hereda el CRUD completo de {@link AbstractCrudService}
 * (Template Method) y solo define los pasos propios de la entidad.
 *
 * Patrón SINGLETON: {@code @Service} registra una única instancia en el
 * contenedor de Spring, compartida por toda la aplicación.
 * Inyección de dependencias: el repositorio y el publicador de eventos llegan
 * por el constructor.
 */
@Service
public class UsuarioService extends AbstractCrudService<Usuario, Long> {

    public UsuarioService(UsuarioRepository repositorio, ApplicationEventPublisher publicadorEventos) {
        super(repositorio, publicadorEventos);
    }

    @Override
    protected String nombreRecurso() {
        return "usuario";
    }

    @Override
    protected void copiarDatos(Usuario origen, Usuario destino) {
        destino.setNombre(origen.getNombre());
    }
}
