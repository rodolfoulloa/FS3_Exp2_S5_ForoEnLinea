package cl.duoc.foro.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para modificar el perfil (PUT /usuarios/{id}). No incluye la contraseña
 * ni el rol: tienen endpoints propios.
 */
public record UsuarioRequest(
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

        @Size(max = 300, message = "La biografía admite hasta 300 caracteres")
        String biografia) {
}
