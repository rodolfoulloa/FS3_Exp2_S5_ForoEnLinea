package cl.duoc.foro.usuarios.dto;

/** DTO de salida de un rol, con la cantidad de usuarios que lo tienen. */
public record RolResponse(Long id, String nombre, String descripcion, long usuarios) {
}
