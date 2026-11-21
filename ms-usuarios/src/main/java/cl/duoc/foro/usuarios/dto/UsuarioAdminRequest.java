package cl.duoc.foro.usuarios.dto;

import cl.duoc.foro.usuarios.validation.ContrasenaSegura;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO para que un ADMIN cree un usuario con el rol indicado (POST /usuarios). */
public record UsuarioAdminRequest(
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

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "^(USUARIO|ADMIN)$", message = "El rol debe ser USUARIO o ADMIN")
        String rol) {
}
