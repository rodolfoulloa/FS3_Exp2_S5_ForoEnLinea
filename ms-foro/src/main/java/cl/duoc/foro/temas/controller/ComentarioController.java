package cl.duoc.foro.temas.controller;

import cl.duoc.foro.temas.dto.ForoDtos.ComentarioRequest;
import cl.duoc.foro.temas.dto.ForoDtos.ComentarioResponse;
import cl.duoc.foro.temas.dto.ForoDtos.ModeracionRequest;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.service.ComentarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Edición, eliminación y moderación de comentarios. */
@RestController
@RequestMapping("/comentarios")
public class ComentarioController {

    private static final String HEADER = TemaController.HEADER;

    private final ComentarioService servicio;
    private final ForoFactory factory;

    public ComentarioController(ComentarioService servicio, ForoFactory factory) {
        this.servicio = servicio;
        this.factory = factory;
    }

    /** GET /comentarios?estado=BANEADO : listado por estado (solo ADMIN). */
    @GetMapping
    public List<ComentarioResponse> listar(@RequestHeader(HEADER) Long solicitanteId,
                                           @RequestParam(defaultValue = "BANEADO") EstadoContenido estado) {
        return servicio.listarPorEstado(estado, solicitanteId).stream().map(factory::crearRespuesta).toList();
    }

    /** GET /comentarios/{id} : un comentario con su estado (lo usa ms-moderacion). */
    @GetMapping("/{id}")
    public ComentarioResponse obtener(@PathVariable Long id) {
        return factory.crearRespuesta(servicio.obtenerPorId(id));
    }

    /** PUT /comentarios/{id} : editar (solo el autor). */
    @PutMapping("/{id}")
    public ComentarioResponse editar(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id,
                                     @Valid @RequestBody ComentarioRequest request) {
        return factory.crearRespuesta(servicio.editar(id, request, solicitanteId));
    }

    /** DELETE /comentarios/{id} : eliminar (el autor o un ADMIN). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id) {
        servicio.eliminar(id, solicitanteId);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /comentarios/{id}/moderacion : banear o restaurar (solo ADMIN; lo invoca ms-moderacion). */
    @PatchMapping("/{id}/moderacion")
    public ComentarioResponse moderar(@RequestHeader(HEADER) Long adminId, @PathVariable Long id,
                                      @Valid @RequestBody ModeracionRequest request) {
        return factory.crearRespuesta(
                servicio.moderar(id, EstadoContenido.valueOf(request.estado()), request.motivo(), adminId));
    }
}
