package cl.duoc.foro.usuarios.controller;

import cl.duoc.foro.usuarios.dto.AuthDtos.LoginRequest;
import cl.duoc.foro.usuarios.dto.AuthDtos.LoginResponse;
import cl.duoc.foro.usuarios.dto.AuthDtos.RecuperarRequest;
import cl.duoc.foro.usuarios.dto.AuthDtos.RecuperarResponse;
import cl.duoc.foro.usuarios.dto.AuthDtos.RestablecerRequest;
import cl.duoc.foro.usuarios.dto.RegistroRequest;
import cl.duoc.foro.usuarios.dto.UsuarioResponse;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registro, inicio de sesión y recuperación de contraseña (endpoints públicos). */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioFactory factory;

    public AuthController(AuthService authService, UsuarioFactory factory) {
        this.authService = authService;
        this.factory = factory;
    }

    /** POST /auth/registro : crea una cuenta con rol USUARIO (201). */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        Usuario creado = authService.registrar(request);
        return ResponseEntity.created(URI.create("/usuarios/" + creado.getId()))
                .body(factory.crearRespuesta(creado));
    }

    /** POST /auth/login : inicio de sesión con email o alias. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario u = authService.login(request.usuario(), request.password());
        return new LoginResponse("Bienvenido/a, " + u.getNombre(), factory.crearRespuesta(u));
    }

    /** POST /auth/recuperar : paso 1, genera el código de recuperación. */
    @PostMapping("/recuperar")
    public RecuperarResponse recuperar(@Valid @RequestBody RecuperarRequest request) {
        return authService.solicitarRecuperacion(request.email());
    }

    /** POST /auth/restablecer : paso 2, guarda la nueva contraseña. */
    @PostMapping("/restablecer")
    public Map<String, String> restablecer(@Valid @RequestBody RestablecerRequest request) {
        authService.restablecer(request.email(), request.codigo(), request.nuevaPassword());
        return Map.of("mensaje", "Contraseña restablecida. Ya puede iniciar sesión");
    }
}
