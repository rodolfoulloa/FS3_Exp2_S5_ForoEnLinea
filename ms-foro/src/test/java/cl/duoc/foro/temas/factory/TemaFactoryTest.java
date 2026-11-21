package cl.duoc.foro.temas.factory;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.foro.temas.dto.TemaRequest;
import cl.duoc.foro.temas.dto.TemaResponse;
import cl.duoc.foro.temas.model.Tema;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas del patrón Factory (y del Builder que usa internamente). */
class TemaFactoryTest {

    private final TemaFactory factory = new TemaFactory();

    @Test
    void crearEntidad_construyeLaEntidadSinIdYConTextosLimpios() {
        Tema entidad = factory.crearEntidad(new TemaRequest("  Ejemplo  "));

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getNombre()).isEqualTo("Ejemplo");
    }

    @Test
    void crearRespuesta_copiaTodosLosAtributos() {
        Tema entidad = Tema.builder().id(5L).nombre("Ejemplo").build();

        assertThat(factory.crearRespuesta(entidad)).isEqualTo(new TemaResponse(5L, "Ejemplo"));
    }

    @Test
    void crearRespuestas_convierteCadaElemento() {
        List<TemaResponse> respuestas = factory.crearRespuestas(List.of(
                Tema.builder().id(1L).nombre("A").build(),
                Tema.builder().id(2L).nombre("B").build()));

        assertThat(respuestas).extracting(TemaResponse::id).containsExactly(1L, 2L);
    }
}
