package cl.duoc.foro.temas.repository;

import cl.duoc.foro.temas.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA (patrón Repository): Spring Data genera la implementación
 * de las operaciones CRUD contra la base de datos Oracle.
 */
@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {
}
