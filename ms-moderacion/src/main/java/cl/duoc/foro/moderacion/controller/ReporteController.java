package cl.duoc.foro.moderacion.controller;

import cl.duoc.foro.moderacion.dto.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ReporteResponse;
import cl.duoc.foro.moderacion.factory.ReporteFactory;
import cl.duoc.foro.moderacion.model.Reporte;
import cl.duoc.foro.moderacion.service.CrudService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST con los 5 endpoints del CRUD en /reportes.
 *
 * Depende de la interfaz {@link CrudService} (no de la implementación) y usa
 * {@link ReporteFactory} para convertir entre DTOs y entidades.
 */
@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final CrudService<Reporte, Long> servicio;
    private final ReporteFactory factory;

    public ReporteController(CrudService<Reporte, Long> servicio, ReporteFactory factory) {
        this.servicio = servicio;
        this.factory = factory;
    }

    /** GET /reportes: lista todos. */
    @GetMapping
    public List<ReporteResponse> listar() {
        return factory.crearRespuestas(servicio.listar());
    }

    /** GET /reportes/{id}: obtiene uno por su ID. */
    @GetMapping("/{id}")
    public ReporteResponse obtenerPorId(@PathVariable Long id) {
        return factory.crearRespuesta(servicio.obtenerPorId(id));
    }

    /** POST /reportes: crea uno nuevo (201 Created). */
    @PostMapping
    public ResponseEntity<ReporteResponse> crear(@Valid @RequestBody ReporteRequest request) {
        Reporte creado = servicio.crear(factory.crearEntidad(request));
        return ResponseEntity.created(URI.create("/reportes/" + creado.getId()))
                .body(factory.crearRespuesta(creado));
    }

    /** PUT /reportes/{id}: actualiza uno existente. */
    @PutMapping("/{id}")
    public ReporteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ReporteRequest request) {
        return factory.crearRespuesta(servicio.actualizar(id, factory.crearEntidad(request)));
    }

    /** DELETE /reportes/{id}: elimina uno (204 No Content). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
