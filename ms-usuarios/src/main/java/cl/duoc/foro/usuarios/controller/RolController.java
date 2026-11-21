package cl.duoc.foro.usuarios.controller;

import cl.duoc.foro.usuarios.dto.RolResponse;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.repository.RolRepository;
import cl.duoc.foro.usuarios.service.UsuarioService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /roles : roles del foro con la cantidad de usuarios de cada uno. */
@RestController
@RequestMapping("/roles")
public class RolController {

    private final RolRepository rolRepository;
    private final UsuarioService usuarioService;
    private final UsuarioFactory factory;

    public RolController(RolRepository rolRepository, UsuarioService usuarioService, UsuarioFactory factory) {
        this.rolRepository = rolRepository;
        this.usuarioService = usuarioService;
        this.factory = factory;
    }

    @GetMapping
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream()
                .map(r -> factory.crearRespuestaRol(r, usuarioService.contarPorRol(r.getNombre())))
                .toList();
    }
}
