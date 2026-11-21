package cl.duoc.foro.moderacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.foro.moderacion.client.UsuarioClient;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.UsuarioRemoto;
import cl.duoc.foro.moderacion.event.EventoEntidad;
import cl.duoc.foro.moderacion.event.TipoEvento;
import cl.duoc.foro.moderacion.exception.AccesoDenegadoException;
import cl.duoc.foro.moderacion.exception.ReglaNegocioException;
import cl.duoc.foro.moderacion.factory.ModeracionFactory;
import cl.duoc.foro.moderacion.model.AccionModeracion;
import cl.duoc.foro.moderacion.model.Enums.TipoAccion;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.repository.AccionModeracionRepository;
import cl.duoc.foro.moderacion.strategy.ModeracionStrategy;
import cl.duoc.foro.moderacion.strategy.ModeracionStrategyResolver;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Banear y restaurar con el patrón Strategy, resolución de reportes y eventos. */
@ExtendWith(MockitoExtension.class)
class ModeracionServiceTest {

    @Mock
    private AccionModeracionRepository accionRepository;

    @Mock
    private ReporteService reporteService;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private ModeracionStrategy estrategiaTema;

    @Mock
    private ModeracionStrategy estrategiaComentario;

    @Mock
    private ApplicationEventPublisher publicador;

    private ModeracionService servicio;
    private final UsuarioRemoto camila = new UsuarioRemoto(1L, "camila.admin", "ADMIN", true);

    @BeforeEach
    void setUp() {
        when(estrategiaTema.tipo()).thenReturn(TipoContenido.TEMA);
        when(estrategiaComentario.tipo()).thenReturn(TipoContenido.COMENTARIO);
        ModeracionStrategyResolver resolver = new ModeracionStrategyResolver(List.of(estrategiaTema, estrategiaComentario));
        servicio = new ModeracionService(accionRepository, reporteService, usuarioClient, resolver,
                new ModeracionFactory(), publicador);
    }

    private ContenidoRemoto tema(String estado) {
        return new ContenidoRemoto(11L, "GANA DINERO RÁPIDO", "Spam", 6L, "tomas.dev", estado, null);
    }

    @Test
    void banearTema_usaLaEstrategiaDeTemasResuelveReportesYRegistraLaAccion() {
        when(usuarioClient.exigirAdmin(1L)).thenReturn(camila);
        when(estrategiaTema.obtener(11L)).thenReturn(tema("ACTIVO"));
        when(reporteService.resolverPendientes(TipoContenido.TEMA, 11L, "camila.admin", "Contenido baneado: Spam evidente"))
                .thenReturn(2);
        when(accionRepository.save(any(AccionModeracion.class))).thenAnswer(inv -> inv.getArgument(0));

        AccionModeracion accion = servicio.banear(TipoContenido.TEMA, 11L, "Spam evidente", 1L);

        verify(estrategiaTema).aplicarEstado(11L, "BANEADO", "Spam evidente", 1L);
        verify(estrategiaComentario, never()).aplicarEstado(any(), anyString(), any(), any());
        assertThat(accion.getAccion()).isEqualTo(TipoAccion.BANEAR);
        assertThat(accion.getReportesResueltos()).isEqualTo(2);
        assertThat(accion.getAdminAlias()).isEqualTo("camila.admin");
        assertThat(accion.getResumenContenido()).isEqualTo("GANA DINERO RÁPIDO");
        ArgumentCaptor<EventoEntidad> evento = ArgumentCaptor.forClass(EventoEntidad.class);
        verify(publicador).publishEvent(evento.capture());
        assertThat(evento.getValue().tipo()).isEqualTo(TipoEvento.CREADO);
    }

    @Test
    void banearComentario_usaLaEstrategiaDeComentarios() {
        when(usuarioClient.exigirAdmin(1L)).thenReturn(camila);
        when(estrategiaComentario.obtener(16L))
                .thenReturn(new ContenidoRemoto(16L, null, "Insultos", 6L, "tomas.dev", "ACTIVO", null));
        when(accionRepository.save(any(AccionModeracion.class))).thenAnswer(inv -> inv.getArgument(0));

        AccionModeracion accion = servicio.banear(TipoContenido.COMENTARIO, 16L, "Lenguaje ofensivo", 1L);

        verify(estrategiaComentario).aplicarEstado(16L, "BANEADO", "Lenguaje ofensivo", 1L);
        assertThat(accion.getResumenContenido()).isEqualTo("Insultos");
    }

    @Test
    void banear_contenidoYaBaneado_lanzaReglaDeNegocio() {
        when(usuarioClient.exigirAdmin(1L)).thenReturn(camila);
        when(estrategiaTema.obtener(11L)).thenReturn(tema("BANEADO"));

        assertThatThrownBy(() -> servicio.banear(TipoContenido.TEMA, 11L, "Spam evidente", 1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El tema ya está baneado");
        verify(estrategiaTema, never()).aplicarEstado(any(), anyString(), any(), any());
    }

    @Test
    void banear_sinSerAdmin_noTocaElForo() {
        when(usuarioClient.exigirAdmin(3L)).thenThrow(new AccesoDenegadoException("Esta operación requiere el rol ADMIN"));

        assertThatThrownBy(() -> servicio.banear(TipoContenido.TEMA, 11L, "Spam evidente", 3L))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(estrategiaTema, never()).obtener(any());
    }

    @Test
    void restaurar_temaBaneado_loDejaActivo() {
        when(usuarioClient.exigirAdmin(1L)).thenReturn(camila);
        when(estrategiaTema.obtener(11L)).thenReturn(tema("BANEADO"));
        when(accionRepository.save(any(AccionModeracion.class))).thenAnswer(inv -> inv.getArgument(0));

        AccionModeracion accion = servicio.restaurar(TipoContenido.TEMA, 11L, "Apelación aceptada", 1L);

        verify(estrategiaTema).aplicarEstado(11L, "ACTIVO", null, 1L);
        assertThat(accion.getAccion()).isEqualTo(TipoAccion.RESTAURAR);
    }

    @Test
    void restaurar_temaActivo_lanzaReglaDeNegocio() {
        when(usuarioClient.exigirAdmin(1L)).thenReturn(camila);
        when(estrategiaTema.obtener(11L)).thenReturn(tema("ACTIVO"));

        assertThatThrownBy(() -> servicio.restaurar(TipoContenido.TEMA, 11L, "Apelación aceptada", 1L))
                .hasMessage("El tema no está baneado");
    }

    @Test
    void elHistorialNoSePuedeEditar() {
        AccionModeracion a = AccionModeracion.builder().contenido(TipoContenido.TEMA, 11L, "x")
                .accion(TipoAccion.BANEAR, "Spam").admin(1L, "camila.admin").build();
        when(accionRepository.findById(5L)).thenReturn(java.util.Optional.of(a));

        assertThatThrownBy(() -> servicio.actualizar(5L, a)).isInstanceOf(ReglaNegocioException.class);
    }
}
