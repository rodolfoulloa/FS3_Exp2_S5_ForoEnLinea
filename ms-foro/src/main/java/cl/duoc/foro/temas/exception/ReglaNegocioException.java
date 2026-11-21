package cl.duoc.foro.temas.exception;

/** Se lanza cuando los datos son válidos en forma pero rompen una regla del negocio (HTTP 400). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}