package cl.duoc.foro.usuarios.controller;

import cl.duoc.foro.usuarios.dto.UsuarioRequest;
import cl.duoc.foro.usuarios.dto.UsuarioResponse;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.service.CrudService;
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
 * Controlador REST con los 5 endpoints del CRUD en /usuarios.
 *
 * Depende de la interfaz {@link CrudService} (no de la implementación) y usa
 * {@link UsuarioFactory} para convertir entre DTOs y entidades.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final CrudService<Usuario, Long> servicio;
    private final UsuarioFactory factory;

    public UsuarioController(CrudService<Usuario, Long> servicio, UsuarioFactory factory) {
        this.servicio = servicio;
        this.factory = factory;
    }

    /** GET /usuarios: lista todos. */
    @GetMapping
    public List<UsuarioResponse> listar() {
        return factory.crearRespuestas(servicio.listar());
    }

    /** GET /usuarios/{id}: obtiene uno por su ID. */
    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return factory.crearRespuesta(servicio.obtenerPorId(id));
    }

    /** POST /usuarios: crea uno nuevo (201 Created). */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        Usuario creado = servicio.crear(factory.crearEntidad(request));
        return ResponseEntity.created(URI.create("/usuarios/" + creado.getId()))
                .body(factory.crearRespuesta(creado));
    }

    /** PUT /usuarios/{id}: actualiza uno existente. */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return factory.crearRespuesta(servicio.actualizar(id, factory.crearEntidad(request)));
    }

    /** DELETE /usuarios/{id}: elimina uno (204 No Content). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
