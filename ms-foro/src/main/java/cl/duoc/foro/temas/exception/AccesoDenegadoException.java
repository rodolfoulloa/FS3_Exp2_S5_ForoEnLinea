package cl.duoc.foro.temas.exception;

/** Se lanza cuando el usuario no tiene permisos para la operación (HTTP 403). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}