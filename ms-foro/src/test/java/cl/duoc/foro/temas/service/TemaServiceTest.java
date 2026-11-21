package cl.duoc.foro.temas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.foro.temas.client.UsuarioClient;
import cl.duoc.foro.temas.dto.ForoDtos.TemaRequest;
import cl.duoc.foro.temas.dto.UsuarioRemoto;
import cl.duoc.foro.temas.event.EventoEntidad;
import cl.duoc.foro.temas.event.TipoEvento;
import cl.duoc.foro.temas.exception.AccesoDenegadoException;
import cl.duoc.foro.temas.exception.ContenidoNoDisponibleException;
import cl.duoc.foro.temas.exception.ReglaNegocioException;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.repository.CategoriaRepository;
import cl.duoc.foro.temas.repository.ComentarioRepository;
import cl.duoc.foro.temas.repository.TemaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Reglas del foro para temas: publicación, autoría, visitas, baneo y eventos (Observer). */
@ExtendWith(MockitoExtension.class)
class TemaServiceTest {

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private ApplicationEventPublisher publicador;

    private TemaService servicio;
    private final Categoria tecnologia = new Categoria("Tecnología", "Todo lo digital", "cpu", "#2F6F5E");

    @BeforeEach
    void setUp() {
        servicio = new TemaService(temaRepository, categoriaRepository, comentarioRepository, usuarioClient,
                new ForoFactory(), publicador);
    }

    private Tema tema(Long autorId) {
        return Tema.builder().id(10L).titulo("Título del tema").contenido("Contenido suficiente del tema")
                .categoria(tecnologia).autor(autorId, "vale.soto").build();
    }

    @Test
    void publicar_usaElAliasDeMsUsuariosYPublicaEventoCreado() {
        when(usuarioClient.obtenerActivo(3L)).thenReturn(new UsuarioRemoto(3L, "vale.soto", "USUARIO", true));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(tecnologia));
        when(temaRepository.save(any(Tema.class))).thenAnswer(inv -> {
            Tema t = inv.getArgument(0);
            t.setId(20L);
            return t;
        });

        Tema t = servicio.publicar(new TemaRequest("  ¿Angular o React?  ", "Contenido de prueba suficiente", 1L), 3L);

        assertThat(t.getAutorAlias()).isEqualTo("vale.soto");
        assertThat(t.getTitulo()).isEqualTo("¿Angular o React?");
        assertThat(t.getEstado()).isEqualTo(EstadoContenido.ACTIVO);
        ArgumentCaptor<EventoEntidad> captor = ArgumentCaptor.forClass(EventoEntidad.class);
        verify(publicador).publishEvent(captor.capture());
        assertThat(captor.getValue().tipo()).isEqualTo(TipoEvento.CREADO);
    }

    @Test
    void ver_sumaUnaVisita() {
        Tema t = tema(3L);
        when(temaRepository.findById(10L)).thenReturn(Optional.of(t));
        when(temaRepository.save(t)).thenReturn(t);

        assertThat(servicio.ver(10L).getVistas()).isEqualTo(1);
    }

    @Test
    void ver_temaBaneado_lanza410YNoSumaVisitas() {
        Tema t = tema(3L);
        t.setEstado(EstadoContenido.BANEADO);
        t.setMotivoBaneo("Spam");
        when(temaRepository.findById(10L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> servicio.ver(10L))
                .isInstanceOf(ContenidoNoDisponibleException.class)
                .hasMessageContaining("Spam");
        verify(temaRepository, never()).save(any());
    }

    @Test
    void editar_temaAjeno_lanza403() {
        when(temaRepository.findById(10L)).thenReturn(Optional.of(tema(3L)));

        assertThatThrownBy(() -> servicio.editar(10L, new TemaRequest("Nuevo título largo", "Contenido nuevo suficiente", 1L), 4L))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void editar_temaBaneado_lanzaReglaDeNegocio() {
        Tema t = tema(3L);
        t.setEstado(EstadoContenido.BANEADO);
        when(temaRepository.findById(10L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> servicio.editar(10L, new TemaRequest("Nuevo título largo", "Contenido nuevo suficiente", 1L), 3L))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void eliminar_porOtroUsuario_exigeAdmin() {
        when(temaRepository.findById(10L)).thenReturn(Optional.of(tema(3L)));
        when(usuarioClient.exigirAdmin(4L)).thenThrow(new AccesoDenegadoException("Esta operación requiere el rol ADMIN"));

        assertThatThrownBy(() -> servicio.eliminar(10L, 4L)).isInstanceOf(AccesoDenegadoException.class);
        verify(temaRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_porElAutor_borraComentariosYTema() {
        when(temaRepository.findById(10L)).thenReturn(Optional.of(tema(3L)));
        when(temaRepository.existsById(10L)).thenReturn(true);

        servicio.eliminar(10L, 3L);

        verify(comentarioRepository).deleteByTemaId(10L);
        verify(temaRepository).deleteById(10L);
    }

    @Test
    void moderar_baneaConMotivoYAlRestaurarLoBorra() {
        Tema t = tema(3L);
        when(temaRepository.findById(10L)).thenReturn(Optional.of(t));
        when(temaRepository.save(t)).thenReturn(t);

        servicio.moderar(10L, EstadoContenido.BANEADO, "Spam", 1L);
        assertThat(t.getEstado()).isEqualTo(EstadoContenido.BANEADO);
        assertThat(t.getMotivoBaneo()).isEqualTo("Spam");

        servicio.moderar(10L, EstadoContenido.ACTIVO, null, 1L);
        assertThat(t.getMotivoBaneo()).isNull();
        verify(usuarioClient, org.mockito.Mockito.times(2)).exigirAdmin(1L);
    }

    @Test
    void buscarBaneados_sinSolicitante_lanza403() {
        assertThatThrownBy(() -> servicio.buscar(null, null, null, EstadoContenido.BANEADO, null))
                .isInstanceOf(AccesoDenegadoException.class);
    }
}
