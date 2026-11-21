package cl.duoc.foro.temas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.foro.temas.client.UsuarioClient;
import cl.duoc.foro.temas.dto.ForoDtos.ComentarioRequest;
import cl.duoc.foro.temas.dto.UsuarioRemoto;
import cl.duoc.foro.temas.exception.AccesoDenegadoException;
import cl.duoc.foro.temas.exception.ContenidoNoDisponibleException;
import cl.duoc.foro.temas.exception.ReglaNegocioException;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.repository.ComentarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Reglas del foro para comentarios. */
@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private TemaService temaService;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private ApplicationEventPublisher publicador;

    private ComentarioService servicio;
    private Tema tema;

    @BeforeEach
    void setUp() {
        servicio = new ComentarioService(comentarioRepository, temaService, usuarioClient, new ForoFactory(), publicador);
        tema = Tema.builder().id(10L).titulo("Tema").contenido("Contenido del tema")
                .categoria(new Categoria("Tecnología", "d", "cpu", "#000000")).autor(3L, "vale.soto").build();
    }

    @Test
    void comentar_temaActivo_guardaConElAliasDelAutor() {
        when(temaService.obtenerPorId(10L)).thenReturn(tema);
        when(usuarioClient.obtenerActivo(4L)).thenReturn(new UsuarioRemoto(4L, "mati.gamer", "USUARIO", true));
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));

        Comentario c = servicio.comentar(10L, new ComentarioRequest("  ¡Buen tema!  "), 4L);

        assertThat(c.getAutorAlias()).isEqualTo("mati.gamer");
        assertThat(c.getContenido()).isEqualTo("¡Buen tema!");
    }

    @Test
    void comentar_temaBaneado_lanzaReglaDeNegocio() {
        tema.setEstado(EstadoContenido.BANEADO);
        when(temaService.obtenerPorId(10L)).thenReturn(tema);

        assertThatThrownBy(() -> servicio.comentar(10L, new ComentarioRequest("Hola"), 4L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("No se puede comentar un tema baneado");
        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void listar_deTemaBaneado_lanza410() {
        tema.setEstado(EstadoContenido.BANEADO);
        when(temaService.obtenerPorId(10L)).thenReturn(tema);

        assertThatThrownBy(() -> servicio.listarDeTema(10L, false)).isInstanceOf(ContenidoNoDisponibleException.class);
    }

    @Test
    void listar_soloComentariosActivos() {
        when(temaService.obtenerPorId(10L)).thenReturn(tema);
        when(comentarioRepository.findByTemaIdAndEstadoOrderByFechaAscIdAsc(10L, EstadoContenido.ACTIVO))
                .thenReturn(List.of());

        assertThat(servicio.listarDeTema(10L, false)).isEmpty();
    }

    @Test
    void editar_comentarioAjeno_lanza403() {
        Comentario c = Comentario.builder().id(50L).tema(tema).autor(4L, "mati.gamer").contenido("Hola").build();
        when(comentarioRepository.findById(50L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> servicio.editar(50L, new ComentarioRequest("Cambio"), 3L))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void moderar_exigeAdminYGuardaElMotivo() {
        Comentario c = Comentario.builder().id(50L).tema(tema).autor(4L, "mati.gamer").contenido("Hola").build();
        when(comentarioRepository.findById(50L)).thenReturn(Optional.of(c));
        when(comentarioRepository.save(c)).thenReturn(c);

        servicio.moderar(50L, EstadoContenido.BANEADO, "Lenguaje ofensivo", 1L);

        verify(usuarioClient).exigirAdmin(1L);
        assertThat(c.getEstado()).isEqualTo(EstadoContenido.BANEADO);
        assertThat(c.getMotivoBaneo()).isEqualTo("Lenguaje ofensivo");
    }
}
