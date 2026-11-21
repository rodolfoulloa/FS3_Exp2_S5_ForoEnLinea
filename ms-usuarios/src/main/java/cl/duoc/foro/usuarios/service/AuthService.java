package cl.duoc.foro.usuarios.service;

import cl.duoc.foro.usuarios.dto.AuthDtos.RecuperarResponse;
import cl.duoc.foro.usuarios.dto.RegistroRequest;
import cl.duoc.foro.usuarios.exception.AccesoDenegadoException;
import cl.duoc.foro.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.foro.usuarios.exception.ReglaNegocioException;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicio de sesión, registro y recuperación de contraseña.
 * Reutiliza {@link UsuarioService} para crear el usuario (Template Method,
 * validaciones de unicidad y evento de auditoría).
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MINUTOS_VIGENCIA_CODIGO = 15;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioFactory factory;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioService usuarioService, UsuarioRepository usuarioRepository,
                       UsuarioFactory factory, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.factory = factory;
        this.passwordEncoder = passwordEncoder;
    }

    /** Registro público: el usuario queda con rol USUARIO. */
    @Transactional
    public Usuario registrar(RegistroRequest req) {
        Rol rolUsuario = usuarioService.buscarRol(Rol.USUARIO);
        Usuario nuevo = factory.crearDesdeRegistro(req, rolUsuario, passwordEncoder.encode(req.password()));
        return usuarioService.crear(nuevo);
    }

    /** Login con email o alias. 401 si las credenciales no coinciden, 403 si la cuenta está desactivada. */
    @Transactional(readOnly = true)
    public Usuario login(String credencial, String password) {
        Usuario u = usuarioRepository.buscarPorCredencial(credencial.trim())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            log.warn("Login fallido para {}", credencial);
            throw new CredencialesInvalidasException();
        }
        if (!Boolean.TRUE.equals(u.getActivo())) {
            throw new AccesoDenegadoException("La cuenta " + u.getAlias() + " está desactivada. Contacte a un administrador");
        }
        log.info("Login exitoso: {} ({})", u.getAlias(), u.getRol().getNombre());
        return u;
    }

    /**
     * Paso 1: genera un código de 6 dígitos válido por 15 minutos. Por seguridad
     * la respuesta es la misma exista o no el email (no revela qué cuentas existen).
     */
    @Transactional
    public RecuperarResponse solicitarRecuperacion(String email) {
        Optional<Usuario> usuario = usuarioRepository.findByEmailIgnoreCase(email.trim());
        String mensaje = "Si el email está registrado, se envió un código de recuperación válido por "
                + MINUTOS_VIGENCIA_CODIGO + " minutos";
        if (usuario.isEmpty()) {
            return new RecuperarResponse(mensaje, null, null);
        }
        Usuario u = usuario.get();
        String codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
        LocalDateTime expira = LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA_CODIGO);
        u.setCodigoRecuperacion(codigo);
        u.setCodigoExpira(expira);
        usuarioRepository.save(u);
        log.info("Código de recuperación generado para {}", u.getAlias());
        return new RecuperarResponse(mensaje, codigo, expira);
    }

    /** Paso 2: valida el código y guarda la nueva contraseña. */
    @Transactional
    public void restablecer(String email, String codigo, String nuevaPassword) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ReglaNegocioException("El código no es válido o ya expiró"));
        boolean codigoValido = codigo.equals(u.getCodigoRecuperacion())
                && u.getCodigoExpira() != null
                && u.getCodigoExpira().isAfter(LocalDateTime.now());
        if (!codigoValido) {
            throw new ReglaNegocioException("El código no es válido o ya expiró");
        }
        u.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        u.setCodigoRecuperacion(null);
        u.setCodigoExpira(null);
        usuarioRepository.save(u);
        log.info("Contraseña restablecida para {}", u.getAlias());
    }
}
