package cl.duoc.foro.temas.model;

/**
 * Estado de moderación de un tema o comentario.
 * ACTIVO: visible en el foro. BANEADO: oculto por infringir las normas (lo decide un ADMIN en ms-moderacion).
 */
public enum EstadoContenido {
    ACTIVO,
    BANEADO
}
