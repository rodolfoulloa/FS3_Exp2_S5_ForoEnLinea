package cl.duoc.foro.usuarios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que una contraseña cumpla las 6 reglas de seguridad del foro:
 * <ol>
 *   <li>Entre 8 y 20 caracteres (longitud mínima y máxima).</li>
 *   <li>Al menos una letra mayúscula.</li>
 *   <li>Al menos una letra minúscula.</li>
 *   <li>Al menos un número.</li>
 *   <li>Al menos un carácter especial (por ejemplo ! @ # $ % &amp; *).</li>
 *   <li>Sin espacios en blanco.</li>
 * </ol>
 * El mensaje de error indica la primera regla que no se cumple.
 */
@Documented
@Constraint(validatedBy = ContrasenaSeguraValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContrasenaSegura {

    String message() default "La contraseña no es segura";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
