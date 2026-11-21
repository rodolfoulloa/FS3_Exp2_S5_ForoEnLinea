package cl.duoc.foro.usuarios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;

/** Implementación de {@link ContrasenaSegura}: revisa cada regla y reporta la primera que falla. */
public class ContrasenaSeguraValidator implements ConstraintValidator<ContrasenaSegura, String> {

    public static final int LARGO_MINIMO = 8;
    public static final int LARGO_MAXIMO = 20;

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null) {
            return true; // @NotBlank se encarga del valor vacío
        }
        Optional<String> error = primeraReglaIncumplida(valor);
        if (error.isPresent()) {
            contexto.disableDefaultConstraintViolation();
            contexto.buildConstraintViolationWithTemplate(error.get()).addConstraintViolation();
            return false;
        }
        return true;
    }

    /** Devuelve el mensaje de la primera regla que no se cumple, o vacío si la contraseña es segura. */
    public static Optional<String> primeraReglaIncumplida(String valor) {
        if (valor.length() < LARGO_MINIMO || valor.length() > LARGO_MAXIMO) {
            return Optional.of("La contraseña debe tener entre 8 y 20 caracteres");
        }
        if (valor.chars().noneMatch(Character::isUpperCase)) {
            return Optional.of("La contraseña debe incluir al menos una letra mayúscula");
        }
        if (valor.chars().noneMatch(Character::isLowerCase)) {
            return Optional.of("La contraseña debe incluir al menos una letra minúscula");
        }
        if (valor.chars().noneMatch(Character::isDigit)) {
            return Optional.of("La contraseña debe incluir al menos un número");
        }
        if (valor.chars().allMatch(c -> Character.isLetterOrDigit(c) || Character.isWhitespace(c))) {
            return Optional.of("La contraseña debe incluir al menos un carácter especial (ej: ! @ # $ % & *)");
        }
        if (valor.chars().anyMatch(Character::isWhitespace)) {
            return Optional.of("La contraseña no puede contener espacios");
        }
        return Optional.empty();
    }
}
