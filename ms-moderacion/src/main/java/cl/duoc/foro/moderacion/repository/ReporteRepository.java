package cl.duoc.foro.moderacion.repository;

import cl.duoc.foro.moderacion.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA (patrón Repository): Spring Data genera la implementación
 * de las operaciones CRUD contra la base de datos Oracle.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
}
