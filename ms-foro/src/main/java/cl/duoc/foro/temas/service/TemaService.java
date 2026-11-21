package cl.duoc.foro.temas.service;

import cl.duoc.foro.temas.client.UsuarioClient;
import cl.duoc.foro.temas.dto.ForoDtos.TemaRequest;
import cl.duoc.foro.temas.dto.UsuarioRemoto;
import cl.duoc.foro.temas.event.TipoEvento;
import cl.duoc.foro.temas.exception.AccesoDenegadoException;
import cl.duoc.foro.temas.exception.ContenidoNoDisponibleException;
import cl.duoc.foro.temas.exception.RecursoNoEncontradoException;
import cl.duoc.foro.temas.exception.ReglaNegocioException;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.repository.CategoriaRepository;
import cl.duoc.foro.temas.repository.ComentarioRepository;
import cl.duoc.foro.temas.repository.TemaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Búsqueda, visualización y publicación de temas.
 *
 * Patrón TEMPLATE METHOD: el CRUD lo hereda de {@link AbstractCrudService};
 * aquí se definen los pasos propios (qué se edita de un tema) y las reglas del
 * foro: solo el autor edita su tema, el autor o un ADMIN lo eliminan, y un tema
 * baneado no se muestra (410) ni se puede editar.
 */
@Service
public class TemaService extends AbstractCrudService<Tema, Long> {

    private final TemaRepository temaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioClient usuarioClient;
    private final ForoFactory factory;

    public TemaService(TemaRepository temaRepository, CategoriaRepository categoriaRepository,
                       ComentarioRepository comentarioRepository, UsuarioClient usuarioClient,
                       ForoFactory factory, ApplicationEventPublisher publicadorEventos) {
        super(temaRepository, publicadorEventos);
        this.temaRepository = temaRepository;
        this.categoriaRepository = categoriaRepository;
        this.comentarioRepository = comentarioRepository;
        this.usuarioClient = usuarioClient;
        this.factory = factory;
    }

    // ===== Pasos del Template Method =====

    @Override
    protected String nombreRecurso() {
        return "tema";
    }

    @Override
    protected void copiarDatos(Tema origen, Tema destino) {
        destino.setTitulo(origen.getTitulo());
        destino.setContenido(origen.getContenido());
        destino.setCategoria(origen.getCategoria());
        destino.setFechaActualizacion(LocalDateTime.now());
    }

    // ===== Búsqueda y visualización =====

    /** Búsqueda con filtros opcionales. Listar los baneados requiere ser ADMIN. */
    @Transactional(readOnly = true)
    public List<Tema> buscar(String texto, Long categoriaId, Long autorId, EstadoContenido estado, Long solicitanteId) {
        EstadoContenido filtro = estado == null ? EstadoContenido.ACTIVO : estado;
        if (filtro == EstadoContenido.BANEADO) {
            if (solicitanteId == null) {
                throw new AccesoDenegadoException("Para ver temas baneados envíe el header X-Usuario-Id de un ADMIN");
            }
            usuarioClient.exigirAdmin(solicitanteId);
        }
        String q = texto == null || texto.isBlank() ? null : texto.trim();
        return temaRepository.buscar(q, categoriaId, autorId, filtro);
    }

    /** Ver un tema: suma una visita. Si fue baneado responde 410. */
    @Transactional
    public Tema ver(Long id) {
        Tema t = obtenerPorId(id);
        if (t.estaBaneado()) {
            throw new ContenidoNoDisponibleException("El tema fue removido por moderación: " + t.getMotivoBaneo());
        }
        t.sumarVista();
        return temaRepository.save(t);
    }

    @Transactional(readOnly = true)
    public long contarComentarios(Long temaId) {
        return comentarioRepository.countByTemaIdAndEstado(temaId, EstadoContenido.ACTIVO);
    }

    @Transactional(readOnly = true)
    public Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("categoría", id));
    }

    // ===== Publicación =====

    /** Publica un tema. El autor debe existir en ms-usuarios y estar activo. */
    @Transactional
    public Tema publicar(TemaRequest req, Long autorId) {
        UsuarioRemoto autor = usuarioClient.obtenerActivo(autorId);
        return crear(factory.crearTema(req, buscarCategoria(req.categoriaId()), autor));
    }

    /** Edita un tema: solo su autor y si no está baneado. */
    @Transactional
    public Tema editar(Long id, TemaRequest req, Long solicitanteId) {
        Tema t = obtenerPorId(id);
        if (!t.getAutorId().equals(solicitanteId)) {
            throw new AccesoDenegadoException("Solo el autor puede editar el tema");
        }
        if (t.estaBaneado()) {
            throw new ReglaNegocioException("No se puede editar un tema baneado");
        }
        usuarioClient.obtenerActivo(solicitanteId);
        return actualizar(id, factory.crearDatosTema(req, buscarCategoria(req.categoriaId())));
    }

    /** Elimina un tema y sus comentarios: el autor o un ADMIN. */
    @Transactional
    public void eliminar(Long id, Long solicitanteId) {
        Tema t = obtenerPorId(id);
        if (!t.getAutorId().equals(solicitanteId)) {
            usuarioClient.exigirAdmin(solicitanteId);
        }
        comentarioRepository.deleteByTemaId(id);
        eliminar(id);
    }

    // ===== Moderación (la invoca ms-moderacion) =====

    /** Banea o restaura un tema. Solo un ADMIN. */
    @Transactional
    public Tema moderar(Long id, EstadoContenido estado, String motivo, Long adminId) {
        usuarioClient.exigirAdmin(adminId);
        Tema t = obtenerPorId(id);
        t.setEstado(estado);
        t.setMotivoBaneo(estado == EstadoContenido.BANEADO ? motivo : null);
        Tema guardado = temaRepository.save(t);
        notificar(TipoEvento.ACTUALIZADO, id);
        return guardado;
    }
}
