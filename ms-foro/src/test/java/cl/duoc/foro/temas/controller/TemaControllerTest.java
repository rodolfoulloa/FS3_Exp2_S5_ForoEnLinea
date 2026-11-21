package cl.duoc.foro.temas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.foro.temas.dto.ForoDtos.ComentarioRequest;
import cl.duoc.foro.temas.dto.ForoDtos.TemaRequest;
import cl.duoc.foro.temas.exception.AccesoDenegadoException;
import cl.duoc.foro.temas.exception.ContenidoNoDisponibleException;
import cl.duoc.foro.temas.exception.ServicioNoDisponibleException;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.service.ComentarioService;
import cl.duoc.foro.temas.service.TemaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de /temas sin base de datos ni ms-usuarios (servicios simulados con Mockito). */
@WebMvcTest(TemaController.class)
@Import(ForoFactory.class)
class TemaControllerTest {

    private static final String TEMA_JSON = """
            {"titulo":"¿Angular o React en 2026?","contenido":"Quiero saber qué framework recomiendan para un proyecto nuevo.","categoriaId":1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemaService temaService;

    @MockitoBean
    private ComentarioService comentarioService;

    private final Categoria tecnologia = new Categoria("Tecnología", "Todo lo digital", "cpu", "#2F6F5E");

    private Tema tema() {
        tecnologia.setId(1L);
        return Tema.builder().id(10L).titulo("¿Angular o React en 2026?")
                .contenido("Quiero saber qué framework recomiendan para un proyecto nuevo.")
                .categoria(tecnologia).autor(3L, "vale.soto").build();
    }

    @Test
    void buscar_devuelveTemasConCategoriaYCantidadDeComentarios() throws Exception {
        when(temaService.buscar(eq("angular"), isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(tema()));
        when(temaService.contarComentarios(10L)).thenReturn(3L);

        mockMvc.perform(get("/temas").param("q", "angular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Tecnología"))
                .andExpect(jsonPath("$[0].autorAlias").value("vale.soto"))
                .andExpect(jsonPath("$[0].comentarios").value(3))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    void buscar_conEstadoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/temas").param("estado", "OCULTO")).andExpect(status().isBadRequest());
    }

    @Test
    void ver_temaBaneado_devuelve410() throws Exception {
        when(temaService.ver(11L)).thenThrow(new ContenidoNoDisponibleException("El tema fue removido por moderación: Spam"));

        mockMvc.perform(get("/temas/11"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error").value("El tema fue removido por moderación: Spam"));
    }

    @Test
    void publicar_devuelve201ConLocation() throws Exception {
        when(temaService.publicar(any(TemaRequest.class), eq(3L))).thenReturn(tema());

        mockMvc.perform(post("/temas").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON).content(TEMA_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/temas/10"));
    }

    @Test
    void publicar_conTituloCortoYSinCategoria_devuelve400() throws Exception {
        mockMvc.perform(post("/temas").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Hola\",\"contenido\":\"Muy corto\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.titulo").value("El título debe tener entre 10 y 150 caracteres"))
                .andExpect(jsonPath("$.detalle.contenido").value("El contenido debe tener entre 20 y 2000 caracteres"))
                .andExpect(jsonPath("$.detalle.categoriaId").value("La categoría es obligatoria"));
    }

    @Test
    void publicar_sinHeader_devuelve400() throws Exception {
        mockMvc.perform(post("/temas").contentType(MediaType.APPLICATION_JSON).content(TEMA_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicar_conMsUsuariosCaido_devuelve503() throws Exception {
        when(temaService.publicar(any(TemaRequest.class), anyLong()))
                .thenThrow(new ServicioNoDisponibleException("El microservicio de usuarios no está disponible", null));

        mockMvc.perform(post("/temas").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON).content(TEMA_JSON))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void editar_temaAjeno_devuelve403() throws Exception {
        when(temaService.editar(eq(10L), any(TemaRequest.class), eq(4L)))
                .thenThrow(new AccesoDenegadoException("Solo el autor puede editar el tema"));

        mockMvc.perform(put("/temas/10").header("X-Usuario-Id", 4).contentType(MediaType.APPLICATION_JSON).content(TEMA_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mockMvc.perform(delete("/temas/10").header("X-Usuario-Id", 3)).andExpect(status().isNoContent());
        verify(temaService).eliminar(10L, 3L);
    }

    @Test
    void moderar_conEstadoInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/temas/10/moderacion").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"OCULTO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.estado").value("El estado debe ser ACTIVO o BANEADO"));
    }

    @Test
    void moderar_comoAdmin_baneaElTema() throws Exception {
        Tema baneado = tema();
        baneado.setEstado(EstadoContenido.BANEADO);
        when(temaService.moderar(10L, EstadoContenido.BANEADO, "Spam", 1L)).thenReturn(baneado);

        mockMvc.perform(patch("/temas/10/moderacion").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"BANEADO\",\"motivo\":\"Spam\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BANEADO"));
    }

    @Test
    void comentar_devuelve201() throws Exception {
        Comentario c = Comentario.builder().id(50L).tema(tema()).autor(4L, "mati.gamer").contenido("¡Buen tema!").build();
        when(comentarioService.comentar(eq(10L), any(ComentarioRequest.class), eq(4L))).thenReturn(c);

        mockMvc.perform(post("/temas/10/comentarios").header("X-Usuario-Id", 4).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"¡Buen tema!\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/comentarios/50"))
                .andExpect(jsonPath("$.temaId").value(10))
                .andExpect(jsonPath("$.autorAlias").value("mati.gamer"));
    }

    @Test
    void comentar_vacio_devuelve400() throws Exception {
        mockMvc.perform(post("/temas/10/comentarios").header("X-Usuario-Id", 4).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comentarios_deTemaBaneado_devuelve410() throws Exception {
        doThrow(new ContenidoNoDisponibleException("El tema fue removido por moderación"))
                .when(comentarioService).listarDeTema(11L, false);

        mockMvc.perform(get("/temas/11/comentarios")).andExpect(status().isGone());
    }
}
