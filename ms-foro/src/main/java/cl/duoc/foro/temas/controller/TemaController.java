package cl.duoc.foro.temas.controller;

import cl.duoc.foro.temas.dto.ForoDtos.ComentarioRequest;
import cl.duoc.foro.temas.dto.ForoDtos.ComentarioResponse;
import cl.duoc.foro.temas.dto.ForoDtos.ModeracionRequest;
import cl.duoc.foro.temas.dto.ForoDtos.TemaRequest;
import cl.duoc.foro.temas.dto.ForoDtos.TemaResponse;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.service.ComentarioService;
import cl.duoc.foro.temas.service.TemaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Búsqueda, visualización y publicación de temas, y comentarios de cada tema.
 * Quien publica o modifica se identifica con el header {@code X-Usuario-Id}.
 */
@RestController
@RequestMapping("/temas")
public class TemaController {

    static final String HEADER = "X-Usuario-Id";

    private final TemaService temaService;
    private final ComentarioService comentarioService;
    private final ForoFactory factory;

    public TemaController(TemaService temaService, ComentarioService comentarioService, ForoFactory factory) {
        this.temaService = temaService;
        this.comentarioService = comentarioService;
        this.factory = factory;
    }

    /** GET /temas?q=&categoriaId=&autorId=&estado= : búsqueda (por defecto, temas activos). */
    @GetMapping
    public List<TemaResponse> buscar(@RequestParam(required = false) String q,
                                     @RequestParam(required = false) Long categoriaId,
                                     @RequestParam(required = false) Long autorId,
                                     @RequestParam(required = false) EstadoContenido estado,
                                     @RequestHeader(value = HEADER, required = false) Long solicitanteId) {
        return temaService.buscar(q, categoriaId, autorId, estado, solicitanteId).stream()
                .map(this::respuesta).toList();
    }

    /** GET /temas/{id} : ver un tema (suma una visita; 410 si fue baneado). */
    @GetMapping("/{id}")
    public TemaResponse ver(@PathVariable Long id) {
        return respuesta(temaService.ver(id));
    }

    /** GET /temas/{id}/estado : datos y estado de moderación, sin sumar visitas (lo usa ms-moderacion). */
    @GetMapping("/{id}/estado")
    public TemaResponse estado(@PathVariable Long id) {
        return respuesta(temaService.obtenerPorId(id));
    }

    /** POST /temas : publicar un tema (201). */
    @PostMapping
    public ResponseEntity<TemaResponse> publicar(@RequestHeader(HEADER) Long autorId,
                                                 @Valid @RequestBody TemaRequest request) {
        Tema creado = temaService.publicar(request, autorId);
        return ResponseEntity.created(URI.create("/temas/" + creado.getId())).body(respuesta(creado));
    }

    /** PUT /temas/{id} : editar un tema (solo el autor). */
    @PutMapping("/{id}")
    public TemaResponse editar(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id,
                               @Valid @RequestBody TemaRequest request) {
        return respuesta(temaService.editar(id, request, solicitanteId));
    }

    /** DELETE /temas/{id} : eliminar un tema y sus comentarios (el autor o un ADMIN). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id) {
        temaService.eliminar(id, solicitanteId);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /temas/{id}/moderacion : banear o restaurar (solo ADMIN; lo invoca ms-moderacion). */
    @PatchMapping("/{id}/moderacion")
    public TemaResponse moderar(@RequestHeader(HEADER) Long adminId, @PathVariable Long id,
                                @Valid @RequestBody ModeracionRequest request) {
        return respuesta(temaService.moderar(id, EstadoContenido.valueOf(request.estado()), request.motivo(), adminId));
    }

    /** GET /temas/{id}/comentarios : comentarios visibles del tema. */
    @GetMapping("/{id}/comentarios")
    public List<ComentarioResponse> comentarios(@PathVariable Long id,
                                                @RequestParam(defaultValue = "false") boolean incluirBaneados) {
        return comentarioService.listarDeTema(id, incluirBaneados).stream().map(factory::crearRespuesta).toList();
    }

    /** POST /temas/{id}/comentarios : comentar un tema (201). */
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(@RequestHeader(HEADER) Long autorId, @PathVariable Long id,
                                                       @Valid @RequestBody ComentarioRequest request) {
        Comentario creado = comentarioService.comentar(id, request, autorId);
        return ResponseEntity.created(URI.create("/comentarios/" + creado.getId()))
                .body(factory.crearRespuesta(creado));
    }

    private TemaResponse respuesta(Tema t) {
        return factory.crearRespuesta(t, temaService.contarComentarios(t.getId()));
    }
}
