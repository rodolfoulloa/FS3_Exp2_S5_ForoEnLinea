package cl.duoc.foro.usuarios.dto;

import cl.duoc.foro.usuarios.validation.ContrasenaSegura;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** DTOs de autenticación, recuperación de contraseña y cambios de rol/estado. */
public final class AuthDtos {

    private AuthDtos() {
    }

    /** POST /auth/login: se puede ingresar con email o alias. */
    public record LoginRequest(
            @NotBlank(message = "Ingrese su email o alias")
            @Size(max = 120, message = "El usuario admite hasta 120 caracteres")
            String usuario,

            @NotBlank(message = "Ingrese su contraseña")
            @Size(max = 20, message = "La contraseña admite hasta 20 caracteres")
            String password) {
    }

    /** Respuesta del login: datos del usuario y su rol (el frontend decide qué mostrar). */
    public record LoginResponse(String mensaje, UsuarioResponse usuario) {
    }

    /** POST /auth/recuperar: paso 1, solicitar el código. */
    public record RecuperarRequest(
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El email no tiene un formato válido")
            String email) {
    }

    /**
     * Respuesta del paso 1. Como el envío de correos está fuera del alcance, el
     * código se devuelve en la respuesta para simular que llegó al email.
     */
    public record RecuperarResponse(String mensaje, String codigoSimulado, LocalDateTime expira) {
    }

    /** POST /auth/restablecer: paso 2, nueva contraseña con el código recibido. */
    public record RestablecerRequest(
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El email no tiene un formato válido")
            String email,

            @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "^\\d{6}$", message = "El código debe tener 6 dígitos")
            String codigo,

            @NotBlank(message = "La nueva contraseña es obligatoria")
            @ContrasenaSegura
            String nuevaPassword,

            @NotBlank(message = "Debe confirmar la contraseña")
            String confirmacion) {

        @AssertTrue(message = "Las contraseñas no coinciden")
        public boolean isConfirmacionValida() {
            return nuevaPassword == null || nuevaPassword.equals(confirmacion);
        }
    }

    /** PUT /usuarios/{id}/password: cambio de contraseña desde el perfil. */
    public record CambioPasswordRequest(
            @NotBlank(message = "Ingrese su contraseña actual")
            String actual,

            @NotBlank(message = "La nueva contraseña es obligatoria")
            @ContrasenaSegura
            String nueva,

            @NotBlank(message = "Debe confirmar la contraseña")
            String confirmacion) {

        @AssertTrue(message = "Las contraseñas no coinciden")
        public boolean isConfirmacionValida() {
            return nueva == null || nueva.equals(confirmacion);
        }

        @AssertTrue(message = "La nueva contraseña debe ser distinta de la actual")
        public boolean isDistintaDeLaActual() {
            return nueva == null || !nueva.equals(actual);
        }
    }

    /** PATCH /usuarios/{id}/rol (solo ADMIN). */
    public record CambioRolRequest(
            @NotBlank(message = "El rol es obligatorio")
            @Pattern(regexp = "^(USUARIO|ADMIN)$", message = "El rol debe ser USUARIO o ADMIN")
            String rol) {
    }

    /** PATCH /usuarios/{id}/estado (solo ADMIN): activar o desactivar una cuenta. */
    public record CambioEstadoRequest(
            @NotNull(message = "Debe indicar si el usuario queda activo (true/false)")
            Boolean activo) {
    }
}
