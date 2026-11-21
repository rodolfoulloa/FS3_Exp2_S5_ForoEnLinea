package cl.duoc.foro.moderacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.foro.moderacion.client.UsuarioClient;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.UsuarioRemoto;
import cl.duoc.foro.moderacion.exception.ConflictoException;
import cl.duoc.foro.moderacion.exception.ReglaNegocioException;
import cl.duoc.foro.moderacion.factory.ModeracionFactory;
import cl.duoc.foro.moderacion.model.Enums.EstadoReporte;
import cl.duoc.foro.moderacion.model.Enums.MotivoReporte;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.model.Reporte;
import cl.duoc.foro.moderacion.repository.ReporteRepository;
import cl.duoc.foro.moderacion.strategy.ModeracionStrategy;
import cl.duoc.foro.moderacion.strategy.ModeracionStrategyResolver;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Reglas de los reportes de contenido. */
@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private ModeracionStrategy estrategiaTema;

    @Mock
    private ApplicationEventPublisher publicador;

    private ReporteService servicio;
    private final UsuarioRemoto vale = new UsuarioRemoto(3L, "vale.soto", "USUARIO", true);
    private final ReporteRequest request = new ReporteRequest(TipoContenido.TEMA, 20L, MotivoReporte.SPAM, null);

    @BeforeEach
    void setUp() {
        when(estrategiaTema.tipo()).thenReturn(TipoContenido.TEMA);
        servicio = new ReporteService(reporteRepository, usuarioClient,
                new ModeracionStrategyResolver(List.of(estrategiaTema)), new ModeracionFactory(), publicador);
    }

    private ContenidoRemoto tema(Long autorId, String estado) {
        return new ContenidoRemoto(20L, "OFERTA: compra seguidores", "Texto", autorId, "mati.gamer", estado, null);
    }

    @Test
    void reportar_guardaElReportePendienteConElResumen() {
        when(usuarioClient.obtenerActivo(3L)).thenReturn(vale);
        when(estrategiaTema.obtener(20L)).thenReturn(tema(4L, "ACTIVO"));
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        Reporte r = servicio.reportar(request, 3L);

        assertThat(r.getEstado()).isEqualTo(EstadoReporte.PENDIENTE);
        assertThat(r.getResumenContenido()).isEqualTo("OFERTA: compra seguidores");
        assertThat(r.getReportanteAlias()).isEqualTo("vale.soto");
    }

    @Test
    void reportar_contenidoPropio_lanzaReglaDeNegocio() {
        when(usuarioClient.obtenerActivo(3L)).thenReturn(vale);
        when(estrategiaTema.obtener(20L)).thenReturn(tema(3L, "ACTIVO"));

        assertThatThrownBy(() -> servicio.reportar(request, 3L)).hasMessage("No puede reportar su propio contenido");
    }

    @Test
    void reportar_contenidoBaneado_lanzaReglaDeNegocio() {
        when(usuarioClient.obtenerActivo(3L)).thenReturn(vale);
        when(estrategiaTema.obtener(20L)).thenReturn(tema(4L, "BANEADO"));

        assertThatThrownBy(() -> servicio.reportar(request, 3L)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void reportar_dosVecesElMismoContenido_lanza409() {
        when(usuarioClient.obtenerActivo(3L)).thenReturn(vale);
        when(estrategiaTema.obtener(20L)).thenReturn(tema(4L, "ACTIVO"));
        when(reporteRepository.existsByTipoContenidoAndContenidoIdAndReportanteIdAndEstado(
                TipoContenido.TEMA, 20L, 3L, EstadoReporte.PENDIENTE)).thenReturn(true);

        assertThatThrownBy(() -> servicio.reportar(request, 3L)).isInstanceOf(ConflictoException.class);
        verify(reporteRepository, never()).save(any());
    }

    @Test
    void descartar_reporteYaResuelto_lanzaReglaDeNegocio() {
        Reporte r = Reporte.builder().contenido(TipoContenido.TEMA, 20L, "x").motivo(MotivoReporte.SPAM, null)
                .reportante(3L, "vale.soto").build();
        r.cerrar(EstadoReporte.RESUELTO, "camila.admin", "Baneado");
        when(usuarioClient.exigirAdmin(1L)).thenReturn(new UsuarioRemoto(1L, "camila.admin", "ADMIN", true));
        when(reporteRepository.findById(9L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> servicio.descartar(9L, "No incumple", 1L)).hasMessage("El reporte ya fue resuelto");
    }

    @Test
    void resolverPendientes_cierraTodosLosReportesDelContenido() {
        Reporte r1 = Reporte.builder().contenido(TipoContenido.TEMA, 20L, "x").motivo(MotivoReporte.SPAM, null)
                .reportante(3L, "vale.soto").build();
        Reporte r2 = Reporte.builder().contenido(TipoContenido.TEMA, 20L, "x").motivo(MotivoReporte.ACOSO, null)
                .reportante(5L, "fer.cine").build();
        when(reporteRepository.findByTipoContenidoAndContenidoIdAndEstado(TipoContenido.TEMA, 20L, EstadoReporte.PENDIENTE))
                .thenReturn(List.of(r1, r2));

        int resueltos = servicio.resolverPendientes(TipoContenido.TEMA, 20L, "camila.admin", "Baneado");

        assertThat(resueltos).isEqualTo(2);
        assertThat(List.of(r1, r2)).allMatch(r -> r.getEstado() == EstadoReporte.RESUELTO
                && "camila.admin".equals(r.getResueltoPorAlias()));
    }
}
