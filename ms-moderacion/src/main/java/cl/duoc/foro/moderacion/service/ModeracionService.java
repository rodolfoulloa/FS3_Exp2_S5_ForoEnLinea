package cl.duoc.foro.moderacion.service;

import cl.duoc.foro.moderacion.client.UsuarioClient;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ResumenResponse;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.UsuarioRemoto;
import cl.duoc.foro.moderacion.exception.ReglaNegocioException;
import cl.duoc.foro.moderacion.factory.ModeracionFactory;
import cl.duoc.foro.moderacion.model.AccionModeracion;
import cl.duoc.foro.moderacion.model.Enums.EstadoReporte;
import cl.duoc.foro.moderacion.model.Enums.TipoAccion;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.repository.AccionModeracionRepository;
import cl.duoc.foro.moderacion.strategy.ModeracionStrategy;
import cl.duoc.foro.moderacion.strategy.ModeracionStrategyResolver;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de publicaciones y comentarios por parte de un administrador:
 * banear o restaurar contenido que incumple las normas del foro.
 *
 * <ul>
 *   <li>STRATEGY: la forma de consultar y moderar depende del tipo de contenido
 *       ({@link ModeracionStrategyResolver}).</li>
 *   <li>TEMPLATE METHOD: cada acción se registra con {@code crear()} heredado de
 *       {@link AbstractCrudService}, que también publica el evento (OBSERVER).</li>
 * </ul>
 */
@Service
public class ModeracionService extends AbstractCrudService<AccionModeracion, Long> {

    private final AccionModeracionRepository accionRepository;
    private final ReporteService reporteService;
    private final UsuarioClient usuarioClient;
    private final ModeracionStrategyResolver estrategias;
    private final ModeracionFactory factory;

    public ModeracionService(AccionModeracionRepository accionRepository, ReporteService reporteService,
                             UsuarioClient usuarioClient, ModeracionStrategyResolver estrategias,
                             ModeracionFactory factory, ApplicationEventPublisher publicadorEventos) {
        super(accionRepository, publicadorEventos);
        this.accionRepository = accionRepository;
        this.reporteService = reporteService;
        this.usuarioClient = usuarioClient;
        this.estrategias = estrategias;
        this.factory = factory;
    }

    @Override
    protected String nombreRecurso() {
        return "registro de moderación";
    }

    /** El historial no se edita: una acción registrada es inmutable. */
    @Override
    protected void copiarDatos(AccionModeracion origen, AccionModeracion destino) {
        throw new ReglaNegocioException("El historial de moderación no se puede modificar");
    }

    /** Banea un tema o comentario y resuelve sus reportes pendientes. */
    @Transactional
    public AccionModeracion banear(TipoContenido tipo, Long contenidoId, String motivo, Long adminId) {
        UsuarioRemoto admin = usuarioClient.exigirAdmin(adminId);
        ModeracionStrategy estrategia = estrategias.para(tipo);
        ContenidoRemoto contenido = estrategia.obtener(contenidoId);
        if (contenido.estaBaneado()) {
            throw new ReglaNegocioException("El " + nombre(tipo) + " ya está baneado");
        }
        estrategia.aplicarEstado(contenidoId, "BANEADO", motivo.trim(), adminId);
        int resueltos = reporteService.resolverPendientes(tipo, contenidoId, admin.alias(),
                "Contenido baneado: " + motivo.trim());
        return crear(factory.crearAccion(tipo, contenido, TipoAccion.BANEAR, motivo, admin, resueltos));
    }

    /** Restaura un tema o comentario baneado (por ejemplo, tras una apelación). */
    @Transactional
    public AccionModeracion restaurar(TipoContenido tipo, Long contenidoId, String motivo, Long adminId) {
        UsuarioRemoto admin = usuarioClient.exigirAdmin(adminId);
        ModeracionStrategy estrategia = estrategias.para(tipo);
        ContenidoRemoto contenido = estrategia.obtener(contenidoId);
        if (!contenido.estaBaneado()) {
            throw new ReglaNegocioException("El " + nombre(tipo) + " no está baneado");
        }
        estrategia.aplicarEstado(contenidoId, "ACTIVO", null, adminId);
        return crear(factory.crearAccion(tipo, contenido, TipoAccion.RESTAURAR, motivo, admin, 0));
    }

    /** Historial de acciones (solo ADMIN), opcionalmente por tipo de contenido. */
    @Transactional(readOnly = true)
    public List<AccionModeracion> historial(TipoContenido tipo, Long adminId) {
        usuarioClient.exigirAdmin(adminId);
        return tipo == null ? accionRepository.findAllByOrderByFechaDesc()
                : accionRepository.findByTipoContenidoOrderByFechaDesc(tipo);
    }

    /** Resumen para el panel de moderación (solo ADMIN). */
    @Transactional(readOnly = true)
    public ResumenResponse resumen(Long adminId) {
        usuarioClient.exigirAdmin(adminId);
        return new ResumenResponse(
                reporteService.contarPorEstado(EstadoReporte.PENDIENTE),
                reporteService.contarPorEstado(EstadoReporte.RESUELTO),
                reporteService.contarPorEstado(EstadoReporte.DESCARTADO),
                accionRepository.countByTipoContenidoAndAccion(TipoContenido.TEMA, TipoAccion.BANEAR),
                accionRepository.countByTipoContenidoAndAccion(TipoContenido.COMENTARIO, TipoAccion.BANEAR),
                accionRepository.countByTipoContenidoAndAccion(TipoContenido.TEMA, TipoAccion.RESTAURAR)
                        + accionRepository.countByTipoContenidoAndAccion(TipoContenido.COMENTARIO, TipoAccion.RESTAURAR));
    }

    private String nombre(TipoContenido tipo) {
        return tipo == TipoContenido.TEMA ? "tema" : "comentario";
    }
}
