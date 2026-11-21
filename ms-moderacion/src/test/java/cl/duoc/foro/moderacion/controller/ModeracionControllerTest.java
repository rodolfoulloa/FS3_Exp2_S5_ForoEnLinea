package cl.duoc.foro.moderacion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.foro.moderacion.dto.ModeracionDtos.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ResumenResponse;
import cl.duoc.foro.moderacion.exception.AccesoDenegadoException;
import cl.duoc.foro.moderacion.exception.ConflictoException;
import cl.duoc.foro.moderacion.exception.ServicioNoDisponibleException;
import cl.duoc.foro.moderacion.factory.ModeracionFactory;
import cl.duoc.foro.moderacion.model.AccionModeracion;
import cl.duoc.foro.moderacion.model.Enums.MotivoReporte;
import cl.duoc.foro.moderacion.model.Enums.TipoAccion;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import cl.duoc.foro.moderacion.model.Reporte;
import cl.duoc.foro.moderacion.service.ModeracionService;
import cl.duoc.foro.moderacion.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de /reportes y /moderacion sin base de datos ni otros microservicios. */
@WebMvcTest({ReporteController.class, ModeracionController.class})
@Import(ModeracionFactory.class)
class ModeracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @MockitoBean
    private ModeracionService moderacionService;

    private Reporte reporte() {
        Reporte r = Reporte.builder().contenido(TipoContenido.TEMA, 20L, "OFERTA: compra seguidores")
                .motivo(MotivoReporte.SPAM, "Vende seguidores").reportante(3L, "vale.soto").build();
        r.setId(7L);
        return r;
    }

    @Test
    void reportar_devuelve201() throws Exception {
        when(reporteService.reportar(any(ReporteRequest.class), eq(3L))).thenReturn(reporte());

        mockMvc.perform(post("/reportes").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoContenido\":\"TEMA\",\"contenidoId\":20,\"motivo\":\"SPAM\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reportes/7"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.reportanteAlias").value("vale.soto"));
    }

    @Test
    void reportar_motivoOtroSinDetalle_devuelve400() throws Exception {
        mockMvc.perform(post("/reportes").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoContenido\":\"TEMA\",\"contenidoId\":20,\"motivo\":\"OTRO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.detalleSuficiente").exists());
    }

    @Test
    void reportar_tipoInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/reportes").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoContenido\":\"USUARIO\",\"contenidoId\":20,\"motivo\":\"SPAM\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportar_duplicado_devuelve409() throws Exception {
        when(reporteService.reportar(any(ReporteRequest.class), eq(3L)))
                .thenThrow(new ConflictoException("Ya reportó este contenido y está pendiente de revisión"));

        mockMvc.perform(post("/reportes").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoContenido\":\"TEMA\",\"contenidoId\":20,\"motivo\":\"SPAM\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void bandeja_comoUsuario_devuelve403() throws Exception {
        when(reporteService.listar(null, 3L)).thenThrow(new AccesoDenegadoException("Esta operación requiere el rol ADMIN"));

        mockMvc.perform(get("/reportes").header("X-Usuario-Id", 3)).andExpect(status().isForbidden());
    }

    @Test
    void descartar_conMotivoCorto_devuelve400() throws Exception {
        mockMvc.perform(patch("/reportes/7/descartar").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"no\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.motivo").value("El motivo debe tener entre 10 y 300 caracteres"));
    }

    @Test
    void banearTema_devuelve201ConLaAccion() throws Exception {
        AccionModeracion a = AccionModeracion.builder().contenido(TipoContenido.TEMA, 20L, "OFERTA")
                .accion(TipoAccion.BANEAR, "Spam: venta de seguidores").admin(1L, "camila.admin").reportesResueltos(2).build();
        when(moderacionService.banear(TipoContenido.TEMA, 20L, "Spam: venta de seguidores", 1L)).thenReturn(a);

        mockMvc.perform(post("/moderacion/temas/20/banear").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Spam: venta de seguidores\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accion").value("BANEAR"))
                .andExpect(jsonPath("$.reportesResueltos").value(2));
    }

    @Test
    void banearComentario_conMsForoCaido_devuelve503() throws Exception {
        when(moderacionService.banear(TipoContenido.COMENTARIO, 16L, "Lenguaje ofensivo", 1L))
                .thenThrow(new ServicioNoDisponibleException("El microservicio del foro no está disponible", null));

        mockMvc.perform(post("/moderacion/comentarios/16/banear").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Lenguaje ofensivo\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("El microservicio del foro no está disponible"));
    }

    @Test
    void resumen_devuelveLosContadores() throws Exception {
        when(moderacionService.resumen(1L)).thenReturn(new ResumenResponse(1, 3, 1, 1, 1, 0));

        mockMvc.perform(get("/moderacion/resumen").header("X-Usuario-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportesPendientes").value(1))
                .andExpect(jsonPath("$.temasBaneados").value(1));
    }

    @Test
    void historial_conTipoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/moderacion/acciones").param("tipo", "USUARIO").header("X-Usuario-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resumen_sinHeader_devuelve400() throws Exception {
        mockMvc.perform(get("/moderacion/resumen")).andExpect(status().isBadRequest());
    }
}
