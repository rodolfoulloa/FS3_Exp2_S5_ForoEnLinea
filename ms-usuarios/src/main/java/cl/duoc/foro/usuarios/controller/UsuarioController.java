package cl.duoc.foro.usuarios.controller;

import cl.duoc.foro.usuarios.dto.AuthDtos.CambioEstadoRequest;
import cl.duoc.foro.usuarios.dto.AuthDtos.CambioPasswordRequest;
import cl.duoc.foro.usuarios.dto.AuthDtos.CambioRolRequest;
import cl.duoc.foro.usuarios.dto.UsuarioAdminRequest;
import cl.duoc.foro.usuarios.dto.UsuarioRequest;
import cl.duoc.foro.usuarios.dto.UsuarioResponse;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.service.AutorizacionService;
import cl.duoc.foro.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
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
 * Control de usuarios: crear, modificar, eliminar y administrar.
 * Las operaciones protegidas reciben el header {@code X-Usuario-Id} con el ID
 * de quien hace la solicitud y se validan según su rol.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    static final String HEADER = "X-Usuario-Id";

    private final UsuarioService servicio;
    private final AutorizacionService autorizacion;
    private final UsuarioFactory factory;

    public UsuarioController(UsuarioService servicio, AutorizacionService autorizacion, UsuarioFactory factory) {
        this.servicio = servicio;
        this.autorizacion = autorizacion;
        this.factory = factory;
    }

    /** GET /usuarios?rol=&q= : lista o busca usuarios (solo ADMIN). */
    @GetMapping
    public List<UsuarioResponse> listar(@RequestHeader(HEADER) Long solicitanteId,
                                        @RequestParam(required = false) String rol,
                                        @RequestParam(required = false) String q) {
        autorizacion.exigirAdmin(solicitanteId);
        return factory.crearRespuestas(servicio.listar(rol, q));
    }

    /** GET /usuarios/{id} : perfil de un usuario (público; lo usa ms-moderacion para validar el rol). */
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return factory.crearRespuesta(servicio.obtenerPorId(id));
    }

    /** POST /usuarios : un ADMIN crea un usuario con el rol indicado (201). */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@RequestHeader(HEADER) Long solicitanteId,
                                                 @Valid @RequestBody UsuarioAdminRequest request) {
        autorizacion.exigirAdmin(solicitanteId);
        Usuario nuevo = factory.crearDesdeAdmin(request, servicio.buscarRol(request.rol()),
                servicio.cifrar(request.password()));
        Usuario creado = servicio.crear(nuevo);
        return ResponseEntity.created(URI.create("/usuarios/" + creado.getId()))
                .body(factory.crearRespuesta(creado));
    }

    /** PUT /usuarios/{id} : modificación de perfil (el dueño de la cuenta o un ADMIN). */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id,
                                      @Valid @RequestBody UsuarioRequest request) {
        autorizacion.exigirPropietarioOAdmin(solicitanteId, id);
        return factory.crearRespuesta(servicio.actualizar(id, factory.crearDatosPerfil(request)));
    }

    /** PUT /usuarios/{id}/password : cambio de contraseña (solo el dueño, con su contraseña actual). */
    @PutMapping("/{id}/password")
    public Map<String, String> cambiarPassword(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id,
                                               @Valid @RequestBody CambioPasswordRequest request) {
        autorizacion.exigirPropietario(solicitanteId, id);
        servicio.cambiarPassword(id, request.actual(), request.nueva());
        return Map.of("mensaje", "Contraseña actualizada correctamente");
    }

    /** PATCH /usuarios/{id}/rol : cambia el rol (solo ADMIN). */
    @PatchMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id,
                                      @Valid @RequestBody CambioRolRequest request) {
        autorizacion.exigirAdmin(solicitanteId);
        return factory.crearRespuesta(servicio.cambiarRol(id, request.rol()));
    }

    /** PATCH /usuarios/{id}/estado : activa o desactiva la cuenta (solo ADMIN). */
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id,
                                         @Valid @RequestBody CambioEstadoRequest request) {
        autorizacion.exigirAdmin(solicitanteId);
        return factory.crearRespuesta(servicio.cambiarEstado(id, request.activo()));
    }

    /** DELETE /usuarios/{id} : elimina la cuenta (el dueño o un ADMIN) (204). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(HEADER) Long solicitanteId, @PathVariable Long id) {
        autorizacion.exigirPropietarioOAdmin(solicitanteId, id);
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
