package cl.duoc.foro.usuarios.exception;

/** Se lanza cuando el email/alias o la contraseña no coinciden (HTTP 401). */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos");
    }

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
