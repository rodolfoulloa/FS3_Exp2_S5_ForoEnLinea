package cl.duoc.foro.moderacion.factory;

import cl.duoc.foro.moderacion.dto.ModeracionDtos.AccionResponse;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ReporteResponse;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.UsuarioRemoto;
import cl.duoc.foro.moderacion.model.AccionModeracion;
import cl.duoc.foro.moderacion.model.Enums.TipoAccion;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.model.Reporte;
import org.springframework.stereotype.Component;

/** Patrón FACTORY: crea reportes, acciones de moderación y sus respuestas JSON. */
@Component
public class ModeracionFactory {

    public Reporte crearReporte(ReporteRequest req, ContenidoRemoto contenido, UsuarioRemoto reportante) {
        String detalle = req.detalle() == null || req.detalle().isBlank() ? null : req.detalle().trim();
        return Reporte.builder()
                .contenido(req.tipoContenido(), req.contenidoId(), contenido.resumen())
                .motivo(req.motivo(), detalle)
                .reportante(reportante.id(), reportante.alias())
                .build();
    }

    public AccionModeracion crearAccion(TipoContenido tipo, ContenidoRemoto contenido, TipoAccion accion,
                                        String motivo, UsuarioRemoto admin, int reportesResueltos) {
        return AccionModeracion.builder()
                .contenido(tipo, contenido.id(), contenido.resumen())
                .accion(accion, motivo.trim())
                .admin(admin.id(), admin.alias())
                .reportesResueltos(reportesResueltos)
                .build();
    }

    public ReporteResponse crearRespuesta(Reporte r) {
        return new ReporteResponse(r.getId(), r.getTipoContenido().name(), r.getContenidoId(), r.getResumenContenido(),
                r.getMotivo().name(), r.getDetalle(), r.getReportanteId(), r.getReportanteAlias(), r.getFecha(),
                r.getEstado().name(), r.getResueltoPorAlias(), r.getResolucion(), r.getFechaResolucion());
    }

    public AccionResponse crearRespuesta(AccionModeracion a) {
        return new AccionResponse(a.getId(), a.getTipoContenido().name(), a.getContenidoId(), a.getResumenContenido(),
                a.getAccion().name(), a.getMotivo(), a.getAdminId(), a.getAdminAlias(), a.getReportesResueltos(),
                a.getFecha());
    }
}
