package cl.duoc.foro.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.foro.usuarios.dto.AuthDtos.RecuperarResponse;
import cl.duoc.foro.usuarios.dto.RegistroRequest;
import cl.duoc.foro.usuarios.exception.AccesoDenegadoException;
import cl.duoc.foro.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.foro.usuarios.factory.UsuarioFactory;
import cl.duoc.foro.usuarios.model.Rol;
import cl.duoc.foro.usuarios.model.Usuario;
import cl.duoc.foro.usuarios.service.AuthService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de registro, login y recuperación de contraseña. */
@WebMvcTest(AuthController.class)
@Import(UsuarioFactory.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private Usuario camila() {
        return Usuario.builder().id(1L).nombre("Camila Rojas").alias("camila.admin")
                .email("camila@agora.cl").rol(new Rol(Rol.ADMIN, "Admin")).build();
    }

    private String registro(String password, String confirmacion, boolean normas) {
        return """
                {"nombre":"Ana Prueba","alias":"ana.prueba","email":"ana@correo.cl",
                 "password":"%s","confirmacion":"%s","aceptaNormas":%s}
                """.formatted(password, confirmacion, normas);
    }

    @Test
    void registro_ok_devuelve201ConRolUsuario() throws Exception {
        Usuario ana = Usuario.builder().id(7L).nombre("Ana Prueba").alias("ana.prueba")
                .email("ana@correo.cl").rol(new Rol(Rol.USUARIO, "Usuario")).build();
        when(authService.registrar(any(RegistroRequest.class))).thenReturn(ana);

        mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Prueba#2025", "Prueba#2025", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USUARIO"));
    }

    @Test
    void registro_conClaveSinNumero_devuelve400ConLaReglaIncumplida() throws Exception {
        mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Prueba#abcd", "Prueba#abcd", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.password").value("La contraseña debe incluir al menos un número"));
    }

    @Test
    void registro_conConfirmacionDistintaYSinAceptarNormas_devuelve400() throws Exception {
        mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Prueba#2025", "Otra#2025", false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.confirmacionValida").value("Las contraseñas no coinciden"))
                .andExpect(jsonPath("$.detalle.aceptaNormas").value("Debe aceptar las normas del foro"));
    }

    @Test
    void login_ok_devuelveUsuarioYRol() throws Exception {
        when(authService.login("camila.admin", "Admin#2025")).thenReturn(camila());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"camila.admin\",\"password\":\"Admin#2025\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Bienvenido/a, Camila Rojas"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"));
    }

    @Test
    void login_conCredencialesMalas_devuelve401() throws Exception {
        when(authService.login("camila.admin", "mala")).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"camila.admin\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void login_conCuentaDesactivada_devuelve403() throws Exception {
        when(authService.login("tomas.dev", "Foro#2025")).thenThrow(new AccesoDenegadoException("La cuenta tomas.dev está desactivada"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"tomas.dev\",\"password\":\"Foro#2025\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_sinDatos_devuelve400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.usuario").value("Ingrese su email o alias"))
                .andExpect(jsonPath("$.detalle.password").value("Ingrese su contraseña"));
    }

    @Test
    void recuperar_devuelveElCodigoSimulado() throws Exception {
        when(authService.solicitarRecuperacion("vale@correo.cl"))
                .thenReturn(new RecuperarResponse("Código enviado", "123456", LocalDateTime.now().plusMinutes(15)));

        mockMvc.perform(post("/auth/recuperar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"vale@correo.cl\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoSimulado").value("123456"));
    }

    @Test
    void restablecer_conCodigoMalFormado_devuelve400() throws Exception {
        mockMvc.perform(post("/auth/restablecer").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"vale@correo.cl","codigo":"12ab","nuevaPassword":"Nueva#2025","confirmacion":"Nueva#2025"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle.codigo").value("El código debe tener 6 dígitos"));
    }

    @Test
    void restablecer_ok_devuelve200() throws Exception {
        mockMvc.perform(post("/auth/restablecer").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"vale@correo.cl","codigo":"123456","nuevaPassword":"Nueva#2025","confirmacion":"Nueva#2025"}
                                """))
                .andExpect(status().isOk());
        verify(authService).restablecer("vale@correo.cl", "123456", "Nueva#2025");
    }
}
