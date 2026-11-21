package cl.duoc.foro.temas.exception;

/** Se lanza al pedir un tema o comentario que fue baneado por moderación (HTTP 410 Gone). */
public class ContenidoNoDisponibleException extends RuntimeException {

    public ContenidoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
