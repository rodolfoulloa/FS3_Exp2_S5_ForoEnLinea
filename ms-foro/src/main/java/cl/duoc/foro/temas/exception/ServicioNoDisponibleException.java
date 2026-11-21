package cl.duoc.foro.temas.exception;

/** Se lanza cuando otro microservicio del que se depende no responde (HTTP 503). */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}