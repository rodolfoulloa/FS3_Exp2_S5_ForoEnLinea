package cl.duoc.foro.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.foro.usuarios.event.EventoEntidad;
import cl.duoc.foro.usuarios.event.TipoEvento;
import cl.duoc.foro.usuarios.exception.ConflictoException;
import cl.duoc.foro.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.foro.usuarios.exception.ReglaNegocioException;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.repository.RolRepository;
import cl.duoc.foro.usuarios.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Reglas de negocio de usuarios: unicidad, último admin, cambio de contraseña y eventos (Observer). */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private ApplicationEventPublisher publicador;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private UsuarioService servicio;

    private final Rol rolAdmin = new Rol(Rol.ADMIN, "Admin");
    private final Rol rolUsuario = new Rol(Rol.USUARIO, "Usuario");

    @BeforeEach
    void setUp() {
        servicio = new UsuarioService(usuarioRepository, rolRepository, encoder, publicador);
    }

    private Usuario nuevo() {
        return Usuario.builder().nombre("Ana Prueba").alias("ana.prueba").email("ana@correo.cl")
                .passwordHash("hash").rol(rolUsuario).build();
    }

    @Test
    void crear_guardaYPublicaEventoCreado() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });

        Usuario creado = servicio.crear(nuevo());

        assertThat(creado.getId()).isEqualTo(7L);
        ArgumentCaptor<EventoEntidad> captor = ArgumentCaptor.forClass(EventoEntidad.class);
        verify(publicador).publishEvent(captor.capture());
        assertThat(captor.getValue().tipo()).isEqualTo(TipoEvento.CREADO);
        assertThat(captor.getValue().recurso()).isEqualTo("usuario");
    }

    @Test
    void crear_conEmailRepetido_lanza409() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@correo.cl")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(nuevo()))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("El email ana@correo.cl ya está registrado");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crear_conAliasRepetido_lanza409() {
        when(usuarioRepository.existsByAliasIgnoreCase("ana.prueba")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(nuevo())).hasMessage("El alias ana.prueba ya está en uso");
    }

    @Test
    void actualizar_soloCopiaLosDatosDelPerfil() {
        Usuario existente = Usuario.builder().id(3L).nombre("Vale").alias("vale").email("v@c.cl")
                .passwordHash("hash-original").rol(rolUsuario).build();
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);

        Usuario cambios = Usuario.builder().nombre("Valentina Soto").alias("vale.soto")
                .email("valentina@correo.cl").biografia("Nueva bio").passwordHash("otro").rol(rolAdmin).build();
        Usuario resultado = servicio.actualizar(3L, cambios);

        assertThat(resultado.getNombre()).isEqualTo("Valentina Soto");
        assertThat(resultado.getBiografia()).isEqualTo("Nueva bio");
        assertThat(resultado.getPasswordHash()).isEqualTo("hash-original");
        assertThat(resultado.getRol()).isSameAs(rolUsuario);
    }

    @Test
    void cambiarRol_alUltimoAdmin_lanzaReglaDeNegocio() {
        Usuario admin = Usuario.builder().id(1L).nombre("Camila").alias("camila").email("c@a.cl")
                .rol(rolAdmin).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(rolRepository.findByNombreIgnoreCase("USUARIO")).thenReturn(Optional.of(rolUsuario));
        when(usuarioRepository.findByRolNombreIgnoreCaseOrderByIdAsc(Rol.ADMIN)).thenReturn(List.of(admin));

        assertThatThrownBy(() -> servicio.cambiarRol(1L, "USUARIO"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("último administrador");
    }

    @Test
    void cambiarPassword_conActualIncorrecta_lanza401() {
        Usuario u = Usuario.builder().id(3L).passwordHash(encoder.encode("Foro#2025")).rol(rolUsuario).build();
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> servicio.cambiarPassword(3L, "Mala#2025", "Nueva#2025"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void cambiarPassword_ok_guardaElNuevoHash() {
        Usuario u = Usuario.builder().id(3L).passwordHash(encoder.encode("Foro#2025")).rol(rolUsuario).build();
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));

        servicio.cambiarPassword(3L, "Foro#2025", "Nueva#2025");

        assertThat(encoder.matches("Nueva#2025", u.getPasswordHash())).isTrue();
        verify(usuarioRepository).save(u);
    }
}
