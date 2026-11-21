package cl.duoc.foro.temas.controller;

import cl.duoc.foro.temas.dto.TemaRequest;
import cl.duoc.foro.temas.dto.TemaResponse;
import cl.duoc.foro.temas.factory.TemaFactory;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.service.CrudService;
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
 * Controlador REST con los 5 endpoints del CRUD en /temas.
 *
 * Depende de la interfaz {@link CrudService} (no de la implementación) y usa
 * {@link TemaFactory} para convertir entre DTOs y entidades.
 */
@RestController
@RequestMapping("/temas")
public class TemaController {

    private final CrudService<Tema, Long> servicio;
    private final TemaFactory factory;

    public TemaController(CrudService<Tema, Long> servicio, TemaFactory factory) {
        this.servicio = servicio;
        this.factory = factory;
    }

    /** GET /temas: lista todos. */
    @GetMapping
    public List<TemaResponse> listar() {
        return factory.crearRespuestas(servicio.listar());
    }

    /** GET /temas/{id}: obtiene uno por su ID. */
    @GetMapping("/{id}")
    public TemaResponse obtenerPorId(@PathVariable Long id) {
        return factory.crearRespuesta(servicio.obtenerPorId(id));
    }

    /** POST /temas: crea uno nuevo (201 Created). */
    @PostMapping
    public ResponseEntity<TemaResponse> crear(@Valid @RequestBody TemaRequest request) {
        Tema creado = servicio.crear(factory.crearEntidad(request));
        return ResponseEntity.created(URI.create("/temas/" + creado.getId()))
                .body(factory.crearRespuesta(creado));
    }

    /** PUT /temas/{id}: actualiza uno existente. */
    @PutMapping("/{id}")
    public TemaResponse actualizar(@PathVariable Long id, @Valid @RequestBody TemaRequest request) {
        return factory.crearRespuesta(servicio.actualizar(id, factory.crearEntidad(request)));
    }

    /** DELETE /temas/{id}: elimina uno (204 No Content). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
