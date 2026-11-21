package cl.duoc.foro.moderacion.dto;

/**
 * DTO de salida: lo que el microservicio devuelve en formato JSON.
 * Separa el contrato de la API de la entidad JPA.
 */
public record ReporteResponse(Long id, String nombre) {
}
