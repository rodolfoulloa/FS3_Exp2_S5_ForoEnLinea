package cl.duoc.foro.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import cl.duoc.foro.usuarios.dto.AuthDtos.RecuperarResponse;
import cl.duoc.foro.usuarios.exception.AccesoDenegadoException;
import cl.duoc.foro.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.foro.usuarios.exception.ReglaNegocioException;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Login y recuperación de contraseña. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthService authService;
    private Usuario vale;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioService, usuarioRepository, new UsuarioFactory(), encoder);
        vale = Usuario.builder().id(3L).nombre("Valentina").alias("vale.soto").email("valentina@correo.cl")
                .passwordHash(encoder.encode("Foro#2025")).rol(new Rol(Rol.USUARIO, "Usuario")).build();
    }

    @Test
    void login_conAliasYClaveCorrecta_devuelveElUsuario() {
        when(usuarioRepository.buscarPorCredencial("vale.soto")).thenReturn(Optional.of(vale));

        assertThat(authService.login("vale.soto", "Foro#2025")).isSameAs(vale);
    }

    @Test
    void login_conClaveIncorrecta_lanza401() {
        when(usuarioRepository.buscarPorCredencial("vale.soto")).thenReturn(Optional.of(vale));

        assertThatThrownBy(() -> authService.login("vale.soto", "Otra#2025"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void login_conCuentaDesactivada_lanza403() {
        vale.setActivo(false);
        when(usuarioRepository.buscarPorCredencial("vale.soto")).thenReturn(Optional.of(vale));

        assertThatThrownBy(() -> authService.login("vale.soto", "Foro#2025"))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void recuperar_generaUnCodigoDe6DigitosQueExpiraEn15Minutos() {
        when(usuarioRepository.findByEmailIgnoreCase("valentina@correo.cl")).thenReturn(Optional.of(vale));

        RecuperarResponse respuesta = authService.solicitarRecuperacion("valentina@correo.cl");

        assertThat(respuesta.codigoSimulado()).matches("\\d{6}");
        assertThat(vale.getCodigoRecuperacion()).isEqualTo(respuesta.codigoSimulado());
        assertThat(respuesta.expira()).isAfter(LocalDateTime.now().plusMinutes(14));
    }

    @Test
    void recuperar_conEmailInexistente_noRevelaSiLaCuentaExiste() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@correo.cl")).thenReturn(Optional.empty());

        RecuperarResponse respuesta = authService.solicitarRecuperacion("nadie@correo.cl");

        assertThat(respuesta.mensaje()).startsWith("Si el email está registrado");
        assertThat(respuesta.codigoSimulado()).isNull();
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void restablecer_conCodigoCorrecto_cambiaLaClaveYBorraElCodigo() {
        vale.setCodigoRecuperacion("123456");
        vale.setCodigoExpira(LocalDateTime.now().plusMinutes(5));
        when(usuarioRepository.findByEmailIgnoreCase("valentina@correo.cl")).thenReturn(Optional.of(vale));

        authService.restablecer("valentina@correo.cl", "123456", "Nueva#2025");

        assertThat(encoder.matches("Nueva#2025", vale.getPasswordHash())).isTrue();
        assertThat(vale.getCodigoRecuperacion()).isNull();
    }

    @Test
    void restablecer_conCodigoExpirado_lanzaReglaDeNegocio() {
        vale.setCodigoRecuperacion("123456");
        vale.setCodigoExpira(LocalDateTime.now().minusMinutes(1));
        when(usuarioRepository.findByEmailIgnoreCase("valentina@correo.cl")).thenReturn(Optional.of(vale));

        assertThatThrownBy(() -> authService.restablecer("valentina@correo.cl", "123456", "Nueva#2025"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El código no es válido o ya expiró");
    }
}
