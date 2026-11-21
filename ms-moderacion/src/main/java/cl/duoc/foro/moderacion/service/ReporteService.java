package cl.duoc.foro.moderacion.service;

import cl.duoc.foro.moderacion.client.UsuarioClient;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.UsuarioRemoto;
import cl.duoc.foro.moderacion.event.TipoEvento;
import cl.duoc.foro.moderacion.exception.ConflictoException;
import cl.duoc.foro.moderacion.exception.ReglaNegocioException;
import cl.duoc.foro.moderacion.factory.ModeracionFactory;
import cl.duoc.foro.moderacion.model.Enums.EstadoReporte;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.model.Reporte;
import cl.duoc.foro.moderacion.repository.ReporteRepository;
import cl.duoc.foro.moderacion.strategy.ModeracionStrategyResolver;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reportes de contenido. Hereda el CRUD de {@link AbstractCrudService}
 * (patrón TEMPLATE METHOD) y agrega las reglas: solo usuarios activos
 * reportan, no se reporta contenido propio ni ya baneado, y no se repite un
 * reporte pendiente del mismo usuario.
 */
@Service
public class ReporteService extends AbstractCrudService<Reporte, Long> {

    private final ReporteRepository reporteRepository;
    private final UsuarioClient usuarioClient;
    private final ModeracionStrategyResolver estrategias;
    private final ModeracionFactory factory;

    public ReporteService(ReporteRepository reporteRepository, UsuarioClient usuarioClient,
                          ModeracionStrategyResolver estrategias, ModeracionFactory factory,
                          ApplicationEventPublisher publicadorEventos) {
        super(reporteRepository, publicadorEventos);
        this.reporteRepository = reporteRepository;
        this.usuarioClient = usuarioClient;
        this.estrategias = estrategias;
        this.factory = factory;
    }

    @Override
    protected String nombreRecurso() {
        return "reporte";
    }

    @Override
    protected void copiarDatos(Reporte origen, Reporte destino) {
        destino.setDetalle(origen.getDetalle());
    }

    /** Un usuario reporta un tema o comentario. */
    @Transactional
    public Reporte reportar(ReporteRequest req, Long reportanteId) {
        UsuarioRemoto reportante = usuarioClient.obtenerActivo(reportanteId);
        ContenidoRemoto contenido = estrategias.para(req.tipoContenido()).obtener(req.contenidoId());
        if (contenido.estaBaneado()) {
            throw new ReglaNegocioException("El contenido ya fue moderado y no está visible en el foro");
        }
        if (reportanteId.equals(contenido.autorId())) {
            throw new ReglaNegocioException("No puede reportar su propio contenido");
        }
        if (reporteRepository.existsByTipoContenidoAndContenidoIdAndReportanteIdAndEstado(
                req.tipoContenido(), req.contenidoId(), reportanteId, EstadoReporte.PENDIENTE)) {
            throw new ConflictoException("Ya reportó este contenido y está pendiente de revisión");
        }
        return crear(factory.crearReporte(req, contenido, reportante));
    }

    /** Bandeja de reportes (solo ADMIN), opcionalmente filtrada por estado. */
    @Transactional(readOnly = true)
    public List<Reporte> listar(EstadoReporte estado, Long adminId) {
        usuarioClient.exigirAdmin(adminId);
        return estado == null ? reporteRepository.findAllByOrderByFechaDesc()
                : reporteRepository.findByEstadoOrderByFechaDesc(estado);
    }

    @Transactional(readOnly = true)
    public Reporte obtener(Long id, Long adminId) {
        usuarioClient.exigirAdmin(adminId);
        return obtenerPorId(id);
    }

    /** El ADMIN descarta un reporte que no incumple las normas. */
    @Transactional
    public Reporte descartar(Long id, String motivo, Long adminId) {
        UsuarioRemoto admin = usuarioClient.exigirAdmin(adminId);
        Reporte r = obtenerPorId(id);
        if (!r.estaPendiente()) {
            throw new ReglaNegocioException("El reporte ya fue " + r.getEstado().name().toLowerCase());
        }
        r.cerrar(EstadoReporte.DESCARTADO, admin.alias(), motivo.trim());
        Reporte guardado = reporteRepository.save(r);
        notificar(TipoEvento.ACTUALIZADO, id);
        return guardado;
    }

    /** Marca como RESUELTOS los reportes pendientes de un contenido que se baneó. Devuelve cuántos. */
    @Transactional
    public int resolverPendientes(TipoContenido tipo, Long contenidoId, String adminAlias, String resolucion) {
        List<Reporte> pendientes = reporteRepository.findByTipoContenidoAndContenidoIdAndEstado(
                tipo, contenidoId, EstadoReporte.PENDIENTE);
        pendientes.forEach(r -> {
            r.cerrar(EstadoReporte.RESUELTO, adminAlias, resolucion);
            reporteRepository.save(r);
            notificar(TipoEvento.ACTUALIZADO, r.getId());
        });
        return pendientes.size();
    }

    public long contarPorEstado(EstadoReporte estado) {
        return reporteRepository.countByEstado(estado);
    }
}
