package cl.duoc.foro.moderacion.config;

import cl.duoc.foro.moderacion.model.AccionModeracion;
import cl.duoc.foro.moderacion.model.Enums.EstadoReporte;
import cl.duoc.foro.moderacion.model.Enums.MotivoReporte;
import cl.duoc.foro.moderacion.model.Enums.TipoAccion;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.model.Reporte;
import cl.duoc.foro.moderacion.repository.AccionModeracionRepository;
import cl.duoc.foro.moderacion.repository.ReporteRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Historial inicial coherente con los datos de ms-foro (equivale a
 * sql/03_ms_moderacion.sql): el tema de spam (ID 11) y el comentario ofensivo
 * (ID 16) ya fueron baneados a partir de reportes, y queda un reporte pendiente
 * para revisar en la demo.
 */
@Component
@Profile("!test")
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final ReporteRepository reporteRepository;
    private final AccionModeracionRepository accionRepository;

    public DatosIniciales(ReporteRepository reporteRepository, AccionModeracionRepository accionRepository) {
        this.reporteRepository = reporteRepository;
        this.accionRepository = accionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (reporteRepository.count() > 0 || accionRepository.count() > 0) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now();
        String spam = "GANA DINERO RÁPIDO DESDE CASA haciendo clic aquí";
        String ofensivo = "Comentario con insultos hacia otro usuario (contenido moderado).";

        Reporte r1 = reporte(TipoContenido.TEMA, 11L, spam, MotivoReporte.SPAM, "Publicidad engañosa", 3L, "vale.soto", ahora.minusHours(22));
        r1.cerrar(EstadoReporte.RESUELTO, "camila.admin", "Contenido baneado: Spam: publicidad engañosa");
        Reporte r2 = reporte(TipoContenido.TEMA, 11L, spam, MotivoReporte.SPAM, null, 5L, "fer.cine", ahora.minusHours(21));
        r2.cerrar(EstadoReporte.RESUELTO, "camila.admin", "Contenido baneado: Spam: publicidad engañosa");
        Reporte r3 = reporte(TipoContenido.COMENTARIO, 16L, ofensivo, MotivoReporte.OFENSIVO,
                "Insulta a quien hizo la pregunta", 3L, "vale.soto", ahora.minusDays(2));
        r3.cerrar(EstadoReporte.RESUELTO, "diego.mod", "Contenido baneado: Lenguaje ofensivo hacia otro usuario");
        Reporte r4 = reporte(TipoContenido.COMENTARIO, 10L, "Mi teoría es que todo fue un sueño. No me odien.",
                MotivoReporte.FUERA_DE_TEMA, null, 5L, "fer.cine", ahora.minusDays(1));
        r4.cerrar(EstadoReporte.DESCARTADO, "diego.mod", "Es una opinión sobre el tema; no incumple las normas");
        reporte(TipoContenido.TEMA, 7L, "Rutina de running para principiantes", MotivoReporte.OTRO,
                "Creo que debería estar en la categoría Salud, no en Deportes", 6L, "tomas.dev", ahora.minusHours(5));
        reporteRepository.saveAll(java.util.List.of(r1, r2, r3, r4));

        accionRepository.save(AccionModeracion.builder()
                .contenido(TipoContenido.TEMA, 11L, spam).accion(TipoAccion.BANEAR, "Spam: publicidad engañosa")
                .admin(1L, "camila.admin").reportesResueltos(2).fecha(ahora.minusHours(20)).build());
        accionRepository.save(AccionModeracion.builder()
                .contenido(TipoContenido.COMENTARIO, 16L, ofensivo).accion(TipoAccion.BANEAR, "Lenguaje ofensivo hacia otro usuario")
                .admin(2L, "diego.mod").reportesResueltos(1).fecha(ahora.minusDays(2).plusHours(3)).build());

        log.info("Datos iniciales de moderación: {} reportes y {} acciones", reporteRepository.count(), accionRepository.count());
    }

    private Reporte reporte(TipoContenido tipo, Long id, String resumen, MotivoReporte motivo, String detalle,
                            Long reportanteId, String alias, LocalDateTime fecha) {
        return reporteRepository.save(Reporte.builder().contenido(tipo, id, resumen).motivo(motivo, detalle)
                .reportante(reportanteId, alias).fecha(fecha).build());
    }
}
