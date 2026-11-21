package cl.duoc.foro.usuarios.factory;

import cl.duoc.foro.usuarios.dto.RegistroRequest;
import cl.duoc.foro.usuarios.dto.RolResponse;
import cl.duoc.foro.usuarios.dto.UsuarioAdminRequest;
import cl.duoc.foro.usuarios.dto.UsuarioRequest;
import cl.duoc.foro.usuarios.dto.UsuarioResponse;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Patrón FACTORY: centraliza la creación de usuarios y de sus respuestas.
 * Normaliza los datos (recorta espacios y pasa el email a minúsculas) para que
 * todas las entradas al sistema queden iguales.
 */
@Component
public class UsuarioFactory {

    /** Usuario nuevo desde el registro público (rol USUARIO). Usa el patrón Builder. */
    public Usuario crearDesdeRegistro(RegistroRequest req, Rol rolUsuario, String passwordHash) {
        return Usuario.builder()
                .nombre(req.nombre().trim())
                .alias(req.alias().trim())
                .email(req.email().trim().toLowerCase())
                .passwordHash(passwordHash)
                .rol(rolUsuario)
                .build();
    }

    /** Usuario nuevo creado por un ADMIN con el rol indicado. */
    public Usuario crearDesdeAdmin(UsuarioAdminRequest req, Rol rol, String passwordHash) {
        return Usuario.builder()
                .nombre(req.nombre().trim())
                .alias(req.alias().trim())
                .email(req.email().trim().toLowerCase())
                .passwordHash(passwordHash)
                .rol(rol)
                .build();
    }

    /** Datos editables del perfil, que el servicio copia sobre el usuario existente. */
    public Usuario crearDatosPerfil(UsuarioRequest req) {
        return Usuario.builder()
                .nombre(req.nombre().trim())
                .alias(req.alias().trim())
                .email(req.email().trim().toLowerCase())
                .biografia(req.biografia() == null || req.biografia().isBlank() ? null : req.biografia().trim())
                .build();
    }

    public UsuarioResponse crearRespuesta(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getAlias(),
                u.getEmail(),
                u.getRol() == null ? null : u.getRol().getNombre(),
                u.getActivo(),
                u.getBiografia(),
                u.getFechaRegistro());
    }

    public List<UsuarioResponse> crearRespuestas(List<Usuario> usuarios) {
        return usuarios.stream().map(this::crearRespuesta).toList();
    }

    public RolResponse crearRespuestaRol(Rol rol, long usuarios) {
        return new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion(), usuarios);
    }
}
