package cl.duoc.foro.moderacion.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Pruebas del observador de auditoría (patrón Observer). */
class AuditoriaListenerTest {

    private final AuditoriaListener listener = new AuditoriaListener();

    @Test
    void registraLosEventosRecibidosEnOrden() {
        listener.alCambiarEntidad(EventoEntidad.de(TipoEvento.CREADO, "reporte", 1L));
        listener.alCambiarEntidad(EventoEntidad.de(TipoEvento.ELIMINADO, "reporte", 1L));

        assertThat(listener.getUltimosEventos())
                .extracting(EventoEntidad::tipo)
                .containsExactly(TipoEvento.CREADO, TipoEvento.ELIMINADO);
    }

    @Test
    void conservaSoloLosUltimos50Eventos() {
        for (long i = 1; i <= 60; i++) {
            listener.alCambiarEntidad(EventoEntidad.de(TipoEvento.CREADO, "reporte", i));
        }

        assertThat(listener.getUltimosEventos()).hasSize(50);
        assertThat(listener.getUltimosEventos().get(0).id()).isEqualTo(11L);
    }
}
