package cl.duoc.foro.temas.service;

import cl.duoc.foro.temas.client.UsuarioClient;
import cl.duoc.foro.temas.dto.ForoDtos.ComentarioRequest;
import cl.duoc.foro.temas.dto.UsuarioRemoto;
import cl.duoc.foro.temas.event.TipoEvento;
import cl.duoc.foro.temas.exception.AccesoDenegadoException;
import cl.duoc.foro.temas.exception.ContenidoNoDisponibleException;
import cl.duoc.foro.temas.exception.ReglaNegocioException;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.repository.ComentarioRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comentarios de los temas. Patrón TEMPLATE METHOD (hereda el CRUD de
 * {@link AbstractCrudService}) con las reglas del foro: solo se comenta en temas
 * activos, el autor edita su comentario y el autor o un ADMIN lo eliminan.
 */
@Service
public class ComentarioService extends AbstractCrudService<Comentario, Long> {

    private final ComentarioRepository comentarioRepository;
    private final TemaService temaService;
    private final UsuarioClient usuarioClient;
    private final ForoFactory factory;

    public ComentarioService(ComentarioRepository comentarioRepository, TemaService temaService,
                             UsuarioClient usuarioClient, ForoFactory factory,
                             ApplicationEventPublisher publicadorEventos) {
        super(comentarioRepository, publicadorEventos);
        this.comentarioRepository = comentarioRepository;
        this.temaService = temaService;
        this.usuarioClient = usuarioClient;
        this.factory = factory;
    }

    @Override
    protected String nombreRecurso() {
        return "comentario";
    }

    @Override
    protected void copiarDatos(Comentario origen, Comentario destino) {
        destino.setContenido(origen.getContenido());
    }

    /** Comentarios visibles de un tema (o todos, para un ADMIN). */
    @Transactional(readOnly = true)
    public List<Comentario> listarDeTema(Long temaId, boolean incluirBaneados) {
        Tema tema = temaService.obtenerPorId(temaId);
        if (tema.estaBaneado() && !incluirBaneados) {
            throw new ContenidoNoDisponibleException("El tema fue removido por moderación");
        }
        return incluirBaneados
                ? comentarioRepository.findByTemaIdOrderByFechaAscIdAsc(temaId)
                : comentarioRepository.findByTemaIdAndEstadoOrderByFechaAscIdAsc(temaId, EstadoContenido.ACTIVO);
    }

    /** Listado por estado (ej: BANEADO) para el panel de administración. */
    @Transactional(readOnly = true)
    public List<Comentario> listarPorEstado(EstadoContenido estado, Long solicitanteId) {
        usuarioClient.exigirAdmin(solicitanteId);
        return comentarioRepository.findByEstadoOrderByFechaDesc(estado);
    }

    /** Comentar un tema activo. */
    @Transactional
    public Comentario comentar(Long temaId, ComentarioRequest req, Long autorId) {
        Tema tema = temaService.obtenerPorId(temaId);
        if (tema.estaBaneado()) {
            throw new ReglaNegocioException("No se puede comentar un tema baneado");
        }
        UsuarioRemoto autor = usuarioClient.obtenerActivo(autorId);
        return crear(factory.crearComentario(req, tema, autor));
    }

    @Transactional
    public Comentario editar(Long id, ComentarioRequest req, Long solicitanteId) {
        Comentario c = obtenerPorId(id);
        if (!c.getAutorId().equals(solicitanteId)) {
            throw new AccesoDenegadoException("Solo el autor puede editar el comentario");
        }
        if (c.estaBaneado()) {
            throw new ReglaNegocioException("No se puede editar un comentario baneado");
        }
        usuarioClient.obtenerActivo(solicitanteId);
        return actualizar(id, factory.crearDatosComentario(req));
    }

    @Transactional
    public void eliminar(Long id, Long solicitanteId) {
        Comentario c = obtenerPorId(id);
        if (!c.getAutorId().equals(solicitanteId)) {
            usuarioClient.exigirAdmin(solicitanteId);
        }
        eliminar(id);
    }

    /** Banea o restaura un comentario. Solo un ADMIN (lo invoca ms-moderacion). */
    @Transactional
    public Comentario moderar(Long id, EstadoContenido estado, String motivo, Long adminId) {
        usuarioClient.exigirAdmin(adminId);
        Comentario c = obtenerPorId(id);
        c.setEstado(estado);
        c.setMotivoBaneo(estado == EstadoContenido.BANEADO ? motivo : null);
        Comentario guardado = comentarioRepository.save(c);
        notificar(TipoEvento.ACTUALIZADO, id);
        return guardado;
    }
}
