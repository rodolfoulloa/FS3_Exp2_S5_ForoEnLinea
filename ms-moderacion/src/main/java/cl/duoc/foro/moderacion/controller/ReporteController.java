package cl.duoc.foro.moderacion.controller;

import cl.duoc.foro.moderacion.dto.ModeracionDtos.DecisionRequest;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ReporteResponse;
import cl.duoc.foro.moderacion.factory.ModeracionFactory;
import cl.duoc.foro.moderacion.model.Enums.EstadoReporte;
import cl.duoc.foro.moderacion.model.Reporte;
import cl.duoc.foro.moderacion.service.ReporteService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Reportes de contenido: los crea cualquier usuario activo y los gestiona un ADMIN. */
@RestController
@RequestMapping("/reportes")
public class ReporteController {

    static final String HEADER = "X-Usuario-Id";

    private final ReporteService servicio;
    private final ModeracionFactory factory;

    public ReporteController(ReporteService servicio, ModeracionFactory factory) {
        this.servicio = servicio;
        this.factory = factory;
    }

    /** POST /reportes : reportar un tema o comentario (201). */
    @PostMapping
    public ResponseEntity<ReporteResponse> reportar(@RequestHeader(HEADER) Long reportanteId,
                                                    @Valid @RequestBody ReporteRequest request) {
        Reporte creado = servicio.reportar(request, reportanteId);
        return ResponseEntity.created(URI.create("/reportes/" + creado.getId())).body(factory.crearRespuesta(creado));
    }

    /** GET /reportes?estado= : bandeja de reportes (solo ADMIN). */
    @GetMapping
    public List<ReporteResponse> listar(@RequestHeader(HEADER) Long adminId,
                                        @RequestParam(required = false) EstadoReporte estado) {
        return servicio.listar(estado, adminId).stream().map(factory::crearRespuesta).toList();
    }

    /** GET /reportes/{id} (solo ADMIN). */
    @GetMapping("/{id}")
    public ReporteResponse obtener(@RequestHeader(HEADER) Long adminId, @PathVariable Long id) {
        return factory.crearRespuesta(servicio.obtener(id, adminId));
    }

    /** PATCH /reportes/{id}/descartar : el contenido no incumple las normas (solo ADMIN). */
    @PatchMapping("/{id}/descartar")
    public ReporteResponse descartar(@RequestHeader(HEADER) Long adminId, @PathVariable Long id,
                                     @Valid @RequestBody DecisionRequest request) {
        return factory.crearRespuesta(servicio.descartar(id, request.motivo(), adminId));
    }
}
