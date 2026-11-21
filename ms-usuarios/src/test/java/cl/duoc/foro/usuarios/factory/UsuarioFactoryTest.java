package cl.duoc.foro.usuarios.factory;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.foro.usuarios.dto.UsuarioRequest;
import cl.duoc.foro.usuarios.dto.UsuarioResponse;
import cl.duoc.foro.usuarios.model.Usuario;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas del patrón Factory (y del Builder que usa internamente). */
class UsuarioFactoryTest {

    private final UsuarioFactory factory = new UsuarioFactory();

    @Test
    void crearEntidad_construyeLaEntidadSinIdYConTextosLimpios() {
        Usuario entidad = factory.crearEntidad(new UsuarioRequest("  Ejemplo  "));

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getNombre()).isEqualTo("Ejemplo");
    }

    @Test
    void crearRespuesta_copiaTodosLosAtributos() {
        Usuario entidad = Usuario.builder().id(5L).nombre("Ejemplo").build();

        assertThat(factory.crearRespuesta(entidad)).isEqualTo(new UsuarioResponse(5L, "Ejemplo"));
    }

    @Test
    void crearRespuestas_convierteCadaElemento() {
        List<UsuarioResponse> respuestas = factory.crearRespuestas(List.of(
                Usuario.builder().id(1L).nombre("A").build(),
                Usuario.builder().id(2L).nombre("B").build()));

        assertThat(respuestas).extracting(UsuarioResponse::id).containsExactly(1L, 2L);
    }
}
