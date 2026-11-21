package cl.duoc.foro.moderacion.factory;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.foro.moderacion.dto.ReporteRequest;
import cl.duoc.foro.moderacion.dto.ReporteResponse;
import cl.duoc.foro.moderacion.model.Reporte;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas del patrón Factory (y del Builder que usa internamente). */
class ReporteFactoryTest {

    private final ReporteFactory factory = new ReporteFactory();

    @Test
    void crearEntidad_construyeLaEntidadSinIdYConTextosLimpios() {
        Reporte entidad = factory.crearEntidad(new ReporteRequest("  Ejemplo  "));

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getNombre()).isEqualTo("Ejemplo");
    }

    @Test
    void crearRespuesta_copiaTodosLosAtributos() {
        Reporte entidad = Reporte.builder().id(5L).nombre("Ejemplo").build();

        assertThat(factory.crearRespuesta(entidad)).isEqualTo(new ReporteResponse(5L, "Ejemplo"));
    }

    @Test
    void crearRespuestas_convierteCadaElemento() {
        List<ReporteResponse> respuestas = factory.crearRespuestas(List.of(
                Reporte.builder().id(1L).nombre("A").build(),
                Reporte.builder().id(2L).nombre("B").build()));

        assertThat(respuestas).extracting(ReporteResponse::id).containsExactly(1L, 2L);
    }
}
