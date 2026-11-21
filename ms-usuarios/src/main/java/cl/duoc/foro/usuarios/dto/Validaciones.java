package cl.duoc.foro.usuarios.dto;

/** Expresiones regulares y mensajes compartidos por los DTOs de entrada. */
public final class Validaciones {

    /** Nombre real: letras (con tildes), espacios, apóstrofo y guion; entre 3 y 100 caracteres. */
    public static final String NOMBRE = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü' -]{3,100}$";
    public static final String NOMBRE_MSG = "El nombre debe tener entre 3 y 100 letras (sin números ni símbolos)";

    /** Alias público: letras, números, punto y guion bajo; entre 3 y 20 caracteres. */
    public static final String ALIAS = "^[A-Za-z0-9._]{3,20}$";
    public static final String ALIAS_MSG = "El alias debe tener entre 3 y 20 caracteres: letras, números, punto o guion bajo";

    private Validaciones() {
    }
}
