package cl.duoc.foro.usuarios.service;

import cl.duoc.foro.usuarios.exception.AccesoDenegadoException;
import cl.duoc.foro.usuarios.model.Usuario;
import org.springframework.stereotype.Service;

/**
 * Reglas de privilegios por rol. Quien hace la solicitud se identifica con el
 * header {@code X-Usuario-Id} (el ID que devuelve el login).
 * <ul>
 *   <li>ADMIN: administra cualquier usuario.</li>
 *   <li>USUARIO: solo puede ver y modificar su propia cuenta.</li>
 * </ul>
 */
@Service
public class AutorizacionService {

    private final UsuarioService usuarioService;

    public AutorizacionService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Exige que el solicitante sea un ADMIN activo. */
    public Usuario exigirAdmin(Long solicitanteId) {
        Usuario solicitante = solicitanteActivo(solicitanteId);
        if (!solicitante.esAdmin()) {
            throw new AccesoDenegadoException("Esta operación requiere el rol ADMIN");
        }
        return solicitante;
    }

    /** Exige que el solicitante sea el dueño de la cuenta o un ADMIN. */
    public Usuario exigirPropietarioOAdmin(Long solicitanteId, Long usuarioId) {
        Usuario solicitante = solicitanteActivo(solicitanteId);
        if (!solicitante.esAdmin() && !solicitante.getId().equals(usuarioId)) {
            throw new AccesoDenegadoException("Solo puede modificar su propia cuenta");
        }
        return solicitante;
    }

    /** Exige que el solicitante sea el dueño de la cuenta (ni siquiera un ADMIN cambia contraseñas ajenas). */
    public void exigirPropietario(Long solicitanteId, Long usuarioId) {
        solicitanteActivo(solicitanteId);
        if (!solicitanteId.equals(usuarioId)) {
            throw new AccesoDenegadoException("Solo el dueño de la cuenta puede cambiar su contraseña");
        }
    }

    private Usuario solicitanteActivo(Long solicitanteId) {
        Usuario solicitante = usuarioService.obtenerPorId(solicitanteId);
        if (!Boolean.TRUE.equals(solicitante.getActivo())) {
            throw new AccesoDenegadoException("La cuenta del solicitante está desactivada");
        }
        return solicitante;
    }
}
