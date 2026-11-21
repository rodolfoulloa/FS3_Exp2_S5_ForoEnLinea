package cl.duoc.foro.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.foro.usuarios.exception.ConflictoException;
import cl.duoc.foro.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.service.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de los 5 endpoints sin conectarse a Oracle: el servicio se simula con Mockito.
 */
@WebMvcTest(UsuarioController.class)
@Import(UsuarioFactory.class)
class UsuarioControllerTest {

    private static final String JSON_VALIDO = """
            {"nombre":"Ejemplo"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService servicio;

    private Usuario ejemplo() {
        return Usuario.builder().id(1L).nombre("Ejemplo").build();
    }

    @Test
    void listar_devuelve200ConLaLista() throws Exception {
        when(servicio.listar()).thenReturn(List.of(ejemplo()));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Ejemplo"));
    }

    @Test
    void obtenerPorId_devuelve200() throws Exception {
        when(servicio.obtenerPorId(1L)).thenReturn(ejemplo());

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ejemplo"));
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        when(servicio.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("usuario", 99L));

        mockMvc.perform(get("/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe un usuario con ID 99"));
    }

    @Test
    void obtenerPorId_conIdNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/usuarios/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void crear_devuelve201ConLocation() throws Exception {
        when(servicio.crear(any(Usuario.class))).thenReturn(ejemplo());

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/usuarios/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_conDatosInvalidos_devuelve400ConDetalle() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalle.nombre").exists());
    }

    @Test
    void crear_conJsonMalformado_devuelve400() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("{nombre"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_devuelve200() throws Exception {
        when(servicio.actualizar(eq(1L), any(Usuario.class))).thenReturn(ejemplo());

        mockMvc.perform(put("/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mockMvc.perform(delete("/usuarios/1")).andExpect(status().isNoContent());
        verify(servicio).eliminar(1L);
    }

    @Test
    void eliminar_inexistente_devuelve404() throws Exception {
        doThrow(new RecursoNoEncontradoException("usuario", 99L)).when(servicio).eliminar(99L);

        mockMvc.perform(delete("/usuarios/99")).andExpect(status().isNotFound());
    }

    @Test
    void crear_conConflicto_devuelve409() throws Exception {
        when(servicio.crear(any(Usuario.class))).thenThrow(new ConflictoException("Ya existe"));

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe"));
    }
}
