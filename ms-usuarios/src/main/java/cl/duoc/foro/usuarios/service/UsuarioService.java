package cl.duoc.foro.usuarios.service;

import cl.duoc.foro.usuarios.event.TipoEvento;
import cl.duoc.foro.usuarios.exception.ConflictoException;
import cl.duoc.foro.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.foro.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.foro.usuarios.exception.ReglaNegocioException;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.repository.RolRepository;
import cl.duoc.foro.usuarios.repository.UsuarioRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de usuarios. El CRUD (listar, obtener, crear, actualizar, eliminar)
 * lo hereda de {@link AbstractCrudService} (patrón TEMPLATE METHOD) y aquí solo
 * define los pasos propios: qué datos se editan del perfil y la regla de
 * unicidad de email y alias. Además agrega las operaciones de administración.
 *
 * Patrón SINGLETON: {@code @Service} crea una única instancia en Spring.
 */
@Service
public class UsuarioService extends AbstractCrudService<Usuario, Long> {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          PasswordEncoder passwordEncoder, ApplicationEventPublisher publicadorEventos) {
        super(usuarioRepository, publicadorEventos);
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===== Pasos del Template Method =====

    @Override
    protected String nombreRecurso() {
        return "usuario";
    }

    /** En un PUT solo se editan los datos del perfil (no la contraseña ni el rol). */
    @Override
    protected void copiarDatos(Usuario origen, Usuario destino) {
        destino.setNombre(origen.getNombre());
        destino.setAlias(origen.getAlias());
        destino.setEmail(origen.getEmail());
        destino.setBiografia(origen.getBiografia());
    }

    /** El email y el alias no se pueden repetir entre usuarios (HTTP 409). */
    @Override
    protected void validarAntesDeGuardar(Usuario u) {
        boolean emailUsado = u.getId() == null
                ? usuarioRepository.existsByEmailIgnoreCase(u.getEmail())
                : usuarioRepository.existsByEmailIgnoreCaseAndIdNot(u.getEmail(), u.getId());
        if (emailUsado) {
            throw new ConflictoException("El email " + u.getEmail() + " ya está registrado");
        }
        boolean aliasUsado = u.getId() == null
                ? usuarioRepository.existsByAliasIgnoreCase(u.getAlias())
                : usuarioRepository.existsByAliasIgnoreCaseAndIdNot(u.getAlias(), u.getId());
        if (aliasUsado) {
            throw new ConflictoException("El alias " + u.getAlias() + " ya está en uso");
        }
    }

    // ===== Operaciones propias =====

    @Transactional(readOnly = true)
    public List<Usuario> listar(String rol, String texto) {
        if (texto != null && !texto.isBlank()) {
            List<Usuario> encontrados = usuarioRepository.buscarPorTexto(texto.trim());
            return rol == null || rol.isBlank() ? encontrados
                    : encontrados.stream().filter(u -> u.getRol().getNombre().equalsIgnoreCase(rol)).toList();
        }
        if (rol != null && !rol.isBlank()) {
            return usuarioRepository.findByRolNombreIgnoreCaseOrderByIdAsc(rol.trim());
        }
        return listar();
    }

    @Transactional(readOnly = true)
    public Rol buscarRol(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ReglaNegocioException("No existe el rol " + nombre));
    }

    @Transactional
    public Usuario cambiarRol(Long id, String nombreRol) {
        Usuario u = obtenerPorId(id);
        Rol rol = buscarRol(nombreRol);
        if (u.esAdmin() && !rol.esAdmin() && contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("No se puede quitar el rol ADMIN al último administrador activo");
        }
        u.setRol(rol);
        Usuario guardado = usuarioRepository.save(u);
        notificar(TipoEvento.ACTUALIZADO, id);
        return guardado;
    }

    @Transactional
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario u = obtenerPorId(id);
        if (!activo && u.esAdmin() && contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("No se puede desactivar al último administrador activo");
        }
        u.setActivo(activo);
        Usuario guardado = usuarioRepository.save(u);
        notificar(TipoEvento.ACTUALIZADO, id);
        return guardado;
    }

    @Transactional
    public void cambiarPassword(Long id, String actual, String nueva) {
        Usuario u = obtenerPorId(id);
        if (!passwordEncoder.matches(actual, u.getPasswordHash())) {
            throw new CredencialesInvalidasException("La contraseña actual no es correcta");
        }
        u.setPasswordHash(passwordEncoder.encode(nueva));
        usuarioRepository.save(u);
        notificar(TipoEvento.ACTUALIZADO, id);
    }

    /** Antes de eliminar se verifica que no sea el último administrador. */
    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario u = obtenerPorId(id);
        if (u.esAdmin() && contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("No se puede eliminar al último administrador activo");
        }
        super.eliminar(id);
    }

    @Transactional(readOnly = true)
    public long contarPorRol(String rol) {
        return usuarioRepository.findByRolNombreIgnoreCaseOrderByIdAsc(rol).size();
    }

    public String cifrar(String password) {
        return passwordEncoder.encode(password);
    }

    private long contarAdminsActivos() {
        return usuarioRepository.findByRolNombreIgnoreCaseOrderByIdAsc(Rol.ADMIN).stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .count();
    }
}
