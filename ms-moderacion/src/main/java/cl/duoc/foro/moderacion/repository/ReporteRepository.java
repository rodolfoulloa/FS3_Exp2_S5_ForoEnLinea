package cl.duoc.foro.moderacion.repository;

import cl.duoc.foro.moderacion.model.Enums.EstadoReporte;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.model.Reporte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de reportes (patrón Repository). */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByEstadoOrderByFechaDesc(EstadoReporte estado);

    List<Reporte> findAllByOrderByFechaDesc();

    List<Reporte> findByTipoContenidoAndContenidoIdAndEstado(TipoContenido tipo, Long contenidoId, EstadoReporte estado);

    boolean existsByTipoContenidoAndContenidoIdAndReportanteIdAndEstado(TipoContenido tipo, Long contenidoId,
                                                                        Long reportanteId, EstadoReporte estado);

    long countByEstado(EstadoReporte estado);
}
