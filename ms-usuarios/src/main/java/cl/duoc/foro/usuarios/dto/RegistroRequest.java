package cl.duoc.foro.usuarios.dto;

import cl.duoc.foro.usuarios.validation.ContrasenaSegura;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO del registro público (POST /auth/registro). El usuario queda con rol USUARIO.
 * La contraseña debe cumplir las 6 reglas de {@link ContrasenaSegura} y coincidir con la confirmación.
 */
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Pattern(regexp = Validaciones.NOMBRE, message = Validaciones.NOMBRE_MSG)
        String nombre,

        @NotBlank(message = "El alias es obligatorio")
        @Pattern(regexp = Validaciones.ALIAS, message = Validaciones.ALIAS_MSG)
        String alias,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email admite hasta 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @ContrasenaSegura
        String password,

        @NotBlank(message = "Debe confirmar la contraseña")
        String confirmacion,

        @AssertTrue(message = "Debe aceptar las normas del foro")
        Boolean aceptaNormas) {

    /** Las dos contraseñas deben ser iguales. */
    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isConfirmacionValida() {
        return password == null || password.equals(confirmacion);
    }
}
