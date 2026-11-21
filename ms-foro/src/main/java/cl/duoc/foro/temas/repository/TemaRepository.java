package cl.duoc.foro.temas.repository;

import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de temas (patrón Repository) con la búsqueda del foro. */
@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {

    /**
     * Búsqueda con filtros opcionales: texto (en título o contenido), categoría,
     * autor y estado. Los más recientes primero.
     */
    @Query("""
            select t from Tema t
            where t.estado = :estado
              and (:categoriaId is null or t.categoria.id = :categoriaId)
              and (:autorId is null or t.autorId = :autorId)
              and (:texto is null
                   or lower(t.titulo) like lower(concat('%', :texto, '%'))
                   or lower(t.contenido) like lower(concat('%', :texto, '%')))
            order by t.fechaCreacion desc, t.id desc
            """)
    List<Tema> buscar(@Param("texto") String texto,
                      @Param("categoriaId") Long categoriaId,
                      @Param("autorId") Long autorId,
                      @Param("estado") EstadoContenido estado);

    long countByCategoriaIdAndEstado(Long categoriaId, EstadoContenido estado);
}
