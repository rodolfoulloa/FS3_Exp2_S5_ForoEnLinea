package cl.duoc.foro.moderacion.strategy;

import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;

/**
 * Patrón STRATEGY: cada tipo de contenido (tema o comentario) tiene su propia
 * forma de consultarse y moderarse en ms-foro. El servicio de moderación elige
 * la estrategia según el {@link TipoContenido} en tiempo de ejecución, sin
 * if/else por tipo. Para moderar un nuevo tipo basta con agregar otra estrategia.
 */
public interface ModeracionStrategy {

    /** Tipo de contenido que atiende esta estrategia. */
    TipoContenido tipo();

    /** Consulta el contenido y su estado actual. */
    ContenidoRemoto obtener(Long contenidoId);

    /** Cambia el estado del contenido (ACTIVO o BANEADO) en nombre del ADMIN. */
    ContenidoRemoto aplicarEstado(Long contenidoId, String estado, String motivo, Long adminId);
}
