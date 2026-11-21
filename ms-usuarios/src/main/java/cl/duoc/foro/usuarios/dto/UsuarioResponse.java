package cl.duoc.foro.usuarios.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida de un usuario. Nunca incluye la contraseña ni el código de
 * recuperación. Lo usan el frontend y ms-moderacion (para validar el rol).
 */
public record UsuarioResponse(
        Long id,
        String nombre,
        String alias,
        String email,
        String rol,
        Boolean activo,
        String biografia,
        LocalDateTime fechaRegistro) {
}
