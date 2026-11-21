package cl.duoc.foro.temas.dto;

/**
 * DTO de salida: lo que el microservicio devuelve en formato JSON.
 * Separa el contrato de la API de la entidad JPA.
 */
public record TemaResponse(Long id, String nombre) {
}
