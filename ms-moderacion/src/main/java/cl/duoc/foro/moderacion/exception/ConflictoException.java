package cl.duoc.foro.moderacion.exception;

/** Se lanza cuando la operación choca con datos existentes, por ejemplo un valor único repetido (HTTP 409). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}