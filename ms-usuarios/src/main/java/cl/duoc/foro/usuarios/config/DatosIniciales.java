package cl.duoc.foro.usuarios.config;

import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.repository.RolRepository;
import cl.duoc.foro.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga los roles y usuarios de prueba la primera vez que arranca el
 * microservicio (si las tablas están vacías). Así la demo funciona igual en
 * local, en Docker y en la nube. Equivale a sql/01_ms_usuarios.sql.
 *
 * Contraseñas de prueba: Admin#2025 (administradores) y Foro#2025 (usuarios).
 */
@Component
@Profile("!test")
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            rolRepository.save(new Rol(Rol.ADMIN, "Administra usuarios y modera temas y comentarios del foro"));
            rolRepository.save(new Rol(Rol.USUARIO, "Publica temas, comenta, reporta contenido y edita su perfil"));
            log.info("Roles iniciales creados: ADMIN y USUARIO");
        }
        if (usuarioRepository.count() == 0) {
            Rol admin = rolRepository.findByNombreIgnoreCase(Rol.ADMIN).orElseThrow();
            Rol usuario = rolRepository.findByNombreIgnoreCase(Rol.USUARIO).orElseThrow();
            String claveAdmin = passwordEncoder.encode("Admin#2025");
            String claveUsuario = passwordEncoder.encode("Foro#2025");
            guardar("Camila Rojas", "camila.admin", "camila@agora.cl", claveAdmin, admin,
                    "Administradora de Ágora. Velo por que las conversaciones sean respetuosas.");
            guardar("Diego Fuentes", "diego.mod", "diego@agora.cl", claveAdmin, admin,
                    "Moderador de las categorías de tecnología y videojuegos.");
            guardar("Valentina Soto", "vale.soto", "valentina@correo.cl", claveUsuario, usuario,
                    "Desarrolladora web y fan de la ciencia ficción.");
            guardar("Matías Herrera", "mati.gamer", "matias@correo.cl", claveUsuario, usuario,
                    "Jugador de RPG y estrategia.");
            guardar("Fernanda Muñoz", "fer.cine", "fernanda@correo.cl", claveUsuario, usuario,
                    "Crítica de cine amateur.");
            guardar("Tomás Contreras", "tomas.dev", "tomas@correo.cl", claveUsuario, usuario, null);
            log.info("Usuarios de prueba creados: 2 ADMIN y 4 USUARIO");
        }
    }

    private void guardar(String nombre, String alias, String email, String hash, Rol rol, String bio) {
        usuarioRepository.save(Usuario.builder()
                .nombre(nombre).alias(alias).email(email).passwordHash(hash).rol(rol).biografia(bio)
                .build());
    }
}
