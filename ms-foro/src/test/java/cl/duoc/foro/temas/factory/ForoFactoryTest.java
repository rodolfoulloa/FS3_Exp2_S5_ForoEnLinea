package cl.duoc.foro.temas.factory;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.foro.temas.dto.ForoDtos.ComentarioRequest;
import cl.duoc.foro.temas.dto.ForoDtos.TemaRequest;
import cl.duoc.foro.temas.dto.ForoDtos.TemaResponse;
import cl.duoc.foro.temas.dto.UsuarioRemoto;
import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.Tema;
import org.junit.jupiter.api.Test;

/** Patrón Factory (y Builder) del foro. */
class ForoFactoryTest {

    private final ForoFactory factory = new ForoFactory();
    private final Categoria cine = new Categoria("Cine y Series", "Estrenos", "film", "#B5473A");
    private final UsuarioRemoto fer = new UsuarioRemoto(5L, "fer.cine", "USUARIO", true);

    @Test
    void crearTema_limpiaTextosYCopiaElAutor() {
        Tema t = factory.crearTema(new TemaRequest("  Teorías del final  ", "  Contenido del tema de cine  ", 3L), cine, fer);

        assertThat(t.getTitulo()).isEqualTo("Teorías del final");
        assertThat(t.getContenido()).isEqualTo("Contenido del tema de cine");
        assertThat(t.getAutorId()).isEqualTo(5L);
        assertThat(t.getAutorAlias()).isEqualTo("fer.cine");
        assertThat(t.getVistas()).isZero();
        assertThat(t.getFechaCreacion()).isNotNull();
    }

    @Test
    void crearRespuesta_incluyeCategoriaEstadoYComentarios() {
        cine.setId(3L);
        Tema t = factory.crearTema(new TemaRequest("Teorías del final", "Contenido del tema de cine", 3L), cine, fer);

        TemaResponse r = factory.crearRespuesta(t, 7);

        assertThat(r.categoria()).isEqualTo("Cine y Series");
        assertThat(r.categoriaId()).isEqualTo(3L);
        assertThat(r.estado()).isEqualTo("ACTIVO");
        assertThat(r.comentarios()).isEqualTo(7);
    }

    @Test
    void crearComentario_asociaElTemaYElAutor() {
        Tema t = Tema.builder().id(10L).categoria(cine).autor(3L, "vale.soto").build();

        Comentario c = factory.crearComentario(new ComentarioRequest("  Muy bueno  "), t, fer);

        assertThat(c.getTema()).isSameAs(t);
        assertThat(c.getAutorAlias()).isEqualTo("fer.cine");
        assertThat(c.getContenido()).isEqualTo("Muy bueno");
        assertThat(factory.crearRespuesta(c).temaId()).isEqualTo(10L);
    }
}
