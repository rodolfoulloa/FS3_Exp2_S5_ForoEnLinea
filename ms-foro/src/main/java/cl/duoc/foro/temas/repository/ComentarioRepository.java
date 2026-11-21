package cl.duoc.foro.temas.repository;

import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.EstadoContenido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de comentarios (patrón Repository). */
@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByTemaIdOrderByFechaAscIdAsc(Long temaId);

    List<Comentario> findByTemaIdAndEstadoOrderByFechaAscIdAsc(Long temaId, EstadoContenido estado);

    List<Comentario> findByEstadoOrderByFechaDesc(EstadoContenido estado);

    long countByTemaIdAndEstado(Long temaId, EstadoContenido estado);

    void deleteByTemaId(Long temaId);
}
