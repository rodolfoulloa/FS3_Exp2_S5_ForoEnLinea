package cl.duoc.foro.usuarios.repository;

import cl.duoc.foro.usuarios.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA de usuarios (patrón Repository): Spring Data genera las
 * consultas a partir del nombre de los métodos y de las consultas JPQL.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByAliasIgnoreCase(String alias);

    boolean existsByAliasIgnoreCaseAndIdNot(String alias, Long id);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    List<Usuario> findByRolNombreIgnoreCaseOrderByIdAsc(String rol);

    /** Busca por email o alias (inicio de sesión). */
    @Query("select u from Usuario u where lower(u.email) = lower(:credencial) or lower(u.alias) = lower(:credencial)")
    Optional<Usuario> buscarPorCredencial(@Param("credencial") String credencial);

    /** Búsqueda por texto en nombre, alias o email. */
    @Query("""
            select u from Usuario u
            where lower(u.nombre) like lower(concat('%', :texto, '%'))
               or lower(u.alias) like lower(concat('%', :texto, '%'))
               or lower(u.email) like lower(concat('%', :texto, '%'))
            order by u.id
            """)
    List<Usuario> buscarPorTexto(@Param("texto") String texto);
}
