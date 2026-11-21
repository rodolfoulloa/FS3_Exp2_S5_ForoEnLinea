package cl.duoc.foro.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import cl.duoc.foro.usuarios.exception.AccesoDenegadoException;
import cl.duoc.foro.usuarios.exception.ConflictoException;
import cl.duoc.foro.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.service.AutorizacionService;
import cl.duoc.foro.usuarios.service.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de /usuarios sin base de datos: servicio y autorización simulados con Mockito. */
@WebMvcTest(UsuarioController.class)
@Import(UsuarioFactory.class)
class UsuarioControllerTest {

    private static final String PERFIL = """
            {"nombre":"Valentina Soto","alias":"vale.soto","email":"valentina@correo.cl","biografia":"Hola"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService servicio;

    @MockitoBean
    private AutorizacionService autorizacion;

    private Usuario vale() {
        return Usuario.builder().id(3L).nombre("Valentina Soto").alias("vale.soto")
                .email("valentina@correo.cl").rol(new Rol(Rol.USUARIO, "Usuario")).build();
    }

    @Test
    void listar_comoAdmin_devuelve200() throws Exception {
        when(servicio.listar(null, null)).thenReturn(List.of(vale()));

        mockMvc.perform(get("/usuarios").header("X-Usuario-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alias").value("vale.soto"))
                .andExpect(jsonPath("$[0].rol").value("USUARIO"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        verify(autorizacion).exigirAdmin(1L);
    }

    @Test
    void listar_comoUsuario_devuelve403() throws Exception {
        when(autorizacion.exigirAdmin(3L)).thenThrow(new AccesoDenegadoException("Esta operación requiere el rol ADMIN"));

        mockMvc.perform(get("/usuarios").header("X-Usuario-Id", 3))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Esta operación requiere el rol ADMIN"));
    }

    @Test
    void listar_sinHeader_devuelve400() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Falta el header obligatorio 'X-Usuario-Id'"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(servicio.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("usuario", 99L));

        mockMvc.perform(get("/usuarios/99")).andExpect(status().isNotFound());
    }

    @Test
    void crear_comoAdmin_devuelve201() throws Exception {
        when(servicio.buscarRol("ADMIN")).thenReturn(new Rol(Rol.ADMIN, "Admin"));
        when(servicio.cifrar("Clave#2025")).thenReturn("hash");
        when(servicio.crear(any(Usuario.class))).thenReturn(vale());

        mockMvc.perform(post("/usuarios").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Nuevo Admin","alias":"nuevo.admin","email":"nuevo@agora.cl",
                                 "password":"Clave#2025","rol":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/usuarios/3"));
    }

    @Test
    void crear_conRolInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/usuarios").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Nuevo","alias":"nuevo","email":"nuevo@agora.cl",
                                 "password":"Clave#2025","rol":"SUPERADMIN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.rol").value("El rol debe ser USUARIO o ADMIN"));
    }

    @Test
    void actualizarPerfil_devuelve200() throws Exception {
        when(servicio.actualizar(eq(3L), any(Usuario.class))).thenReturn(vale());

        mockMvc.perform(put("/usuarios/3").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON).content(PERFIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Valentina Soto"));
        verify(autorizacion).exigirPropietarioOAdmin(3L, 3L);
    }

    @Test
    void actualizarPerfil_conAliasInvalidoYEmailMalo_devuelve400() throws Exception {
        mockMvc.perform(put("/usuarios/3").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"V4l3\",\"alias\":\"a b\",\"email\":\"no-es-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.nombre").exists())
                .andExpect(jsonPath("$.detalle.alias").exists())
                .andExpect(jsonPath("$.detalle.email").value("El email no tiene un formato válido"));
    }

    @Test
    void actualizarPerfil_conEmailRepetido_devuelve409() throws Exception {
        when(servicio.actualizar(eq(3L), any(Usuario.class)))
                .thenThrow(new ConflictoException("El email valentina@correo.cl ya está registrado"));

        mockMvc.perform(put("/usuarios/3").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON).content(PERFIL))
                .andExpect(status().isConflict());
    }

    @Test
    void cambiarPassword_conClaveDebil_devuelve400() throws Exception {
        mockMvc.perform(put("/usuarios/3/password").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"Foro#2025\",\"nueva\":\"sinmayus1!\",\"confirmacion\":\"sinmayus1!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.nueva").value("La contraseña debe incluir al menos una letra mayúscula"));
    }

    @Test
    void cambiarPassword_ok_devuelve200() throws Exception {
        mockMvc.perform(put("/usuarios/3/password").header("X-Usuario-Id", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"Foro#2025\",\"nueva\":\"Nueva#2025\",\"confirmacion\":\"Nueva#2025\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada correctamente"));
        verify(servicio).cambiarPassword(3L, "Foro#2025", "Nueva#2025");
    }

    @Test
    void cambiarRol_devuelve200() throws Exception {
        when(servicio.cambiarRol(3L, "ADMIN")).thenReturn(vale());

        mockMvc.perform(patch("/usuarios/3/rol").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":\"ADMIN\"}"))
                .andExpect(status().isOk());
        verify(autorizacion).exigirAdmin(1L);
    }

    @Test
    void cambiarEstado_sinValor_devuelve400() throws Exception {
        mockMvc.perform(patch("/usuarios/3/estado").header("X-Usuario-Id", 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mockMvc.perform(delete("/usuarios/3").header("X-Usuario-Id", 3)).andExpect(status().isNoContent());
        verify(servicio).eliminar(3L);
    }

    @Test
    void eliminar_cuentaAjenaComoUsuario_devuelve403() throws Exception {
        doThrow(new AccesoDenegadoException("Solo puede modificar su propia cuenta"))
                .when(autorizacion).exigirPropietarioOAdmin(4L, 3L);

        mockMvc.perform(delete("/usuarios/3").header("X-Usuario-Id", 4)).andExpect(status().isForbidden());
    }
}
