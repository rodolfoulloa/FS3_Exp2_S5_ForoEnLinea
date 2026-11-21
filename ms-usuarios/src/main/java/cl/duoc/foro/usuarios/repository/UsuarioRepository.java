package cl.duoc.foro.usuarios.repository;

import cl.duoc.foro.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA (patrón Repository): Spring Data genera la implementación
 * de las operaciones CRUD contra la base de datos Oracle.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
