package cl.duoc.foro.moderacion.controller;

import cl.duoc.foro.moderacion.dto.ModeracionDtos.AccionResponse;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.DecisionRequest;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ResumenResponse;
import cl.duoc.foro.moderacion.factory.ModeracionFactory;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.service.ModeracionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de publicaciones y comentarios por parte de un ADMIN: banear o
 * restaurar contenido, historial y resumen. Todas las operaciones exigen el
 * rol ADMIN (validado en ms-usuarios con el header X-Usuario-Id).
 */
@RestController
@RequestMapping("/moderacion")
public class ModeracionController {

    private static final String HEADER = ReporteController.HEADER;

    private final ModeracionService servicio;
    private final ModeracionFactory factory;

    public ModeracionController(ModeracionService servicio, ModeracionFactory factory) {
        this.servicio = servicio;
        this.factory = factory;
    }

    /** POST /moderacion/temas/{id}/banear (201: crea un registro en el historial). */
    @PostMapping("/temas/{id}/banear")
    @ResponseStatus(HttpStatus.CREATED)
    public AccionResponse banearTema(@RequestHeader(HEADER) Long adminId, @PathVariable Long id,
                                     @Valid @RequestBody DecisionRequest request) {
        return factory.crearRespuesta(servicio.banear(TipoContenido.TEMA, id, request.motivo(), adminId));
    }

    /** POST /moderacion/temas/{id}/restaurar */
    @PostMapping("/temas/{id}/restaurar")
    @ResponseStatus(HttpStatus.CREATED)
    public AccionResponse restaurarTema(@RequestHeader(HEADER) Long adminId, @PathVariable Long id,
                                        @Valid @RequestBody DecisionRequest request) {
        return factory.crearRespuesta(servicio.restaurar(TipoContenido.TEMA, id, request.motivo(), adminId));
    }

    /** POST /moderacion/comentarios/{id}/banear */
    @PostMapping("/comentarios/{id}/banear")
    @ResponseStatus(HttpStatus.CREATED)
    public AccionResponse banearComentario(@RequestHeader(HEADER) Long adminId, @PathVariable Long id,
                                           @Valid @RequestBody DecisionRequest request) {
        return factory.crearRespuesta(servicio.banear(TipoContenido.COMENTARIO, id, request.motivo(), adminId));
    }

    /** POST /moderacion/comentarios/{id}/restaurar */
    @PostMapping("/comentarios/{id}/restaurar")
    @ResponseStatus(HttpStatus.CREATED)
    public AccionResponse restaurarComentario(@RequestHeader(HEADER) Long adminId, @PathVariable Long id,
                                              @Valid @RequestBody DecisionRequest request) {
        return factory.crearRespuesta(servicio.restaurar(TipoContenido.COMENTARIO, id, request.motivo(), adminId));
    }

    /** GET /moderacion/acciones?tipo= : historial de moderación. */
    @GetMapping("/acciones")
    public List<AccionResponse> historial(@RequestHeader(HEADER) Long adminId,
                                          @RequestParam(required = false) TipoContenido tipo) {
        return servicio.historial(tipo, adminId).stream().map(factory::crearRespuesta).toList();
    }

    /** GET /moderacion/resumen : contadores para el panel del administrador. */
    @GetMapping("/resumen")
    public ResumenResponse resumen(@RequestHeader(HEADER) Long adminId) {
        return servicio.resumen(adminId);
    }
}
