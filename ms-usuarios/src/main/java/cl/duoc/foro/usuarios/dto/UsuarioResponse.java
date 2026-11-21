package cl.duoc.foro.usuarios.dto;

/**
 * DTO de salida: lo que el microservicio devuelve en formato JSON.
 * Separa el contrato de la API de la entidad JPA.
 */
public record UsuarioResponse(Long id, String nombre) {
}
