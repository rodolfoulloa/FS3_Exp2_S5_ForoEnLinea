package cl.duoc.foro.moderacion.repository;

import cl.duoc.foro.moderacion.model.AccionModeracion;
import cl.duoc.foro.moderacion.model.Enums.TipoAccion;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Historial de acciones de moderación (patrón Repository). */
@Repository
public interface AccionModeracionRepository extends JpaRepository<AccionModeracion, Long> {

    List<AccionModeracion> findAllByOrderByFechaDesc();

    List<AccionModeracion> findByTipoContenidoOrderByFechaDesc(TipoContenido tipo);

    long countByTipoContenidoAndAccion(TipoContenido tipo, TipoAccion accion);
}
