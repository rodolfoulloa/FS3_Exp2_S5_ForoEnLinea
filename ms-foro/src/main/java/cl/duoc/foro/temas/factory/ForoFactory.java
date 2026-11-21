package cl.duoc.foro.temas.factory;

import cl.duoc.foro.temas.dto.ForoDtos.CategoriaResponse;
import cl.duoc.foro.temas.dto.ForoDtos.ComentarioRequest;
import cl.duoc.foro.temas.dto.ForoDtos.ComentarioResponse;
import cl.duoc.foro.temas.dto.ForoDtos.TemaRequest;
import cl.duoc.foro.temas.dto.ForoDtos.TemaResponse;
import cl.duoc.foro.temas.dto.UsuarioRemoto;
import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.Tema;
import org.springframework.stereotype.Component;

/**
 * Patrón FACTORY: crea temas, comentarios y sus respuestas JSON a partir de
 * los DTOs (usando el patrón Builder de las entidades).
 */
@Component
public class ForoFactory {

    public Tema crearTema(TemaRequest req, Categoria categoria, UsuarioRemoto autor) {
        return Tema.builder()
                .titulo(req.titulo().trim())
                .contenido(req.contenido().trim())
                .categoria(categoria)
                .autor(autor.id(), autor.alias())
                .build();
    }

    /** Datos editables de un tema, que el servicio copia sobre el existente. */
    public Tema crearDatosTema(TemaRequest req, Categoria categoria) {
        return Tema.builder()
                .titulo(req.titulo().trim())
                .contenido(req.contenido().trim())
                .categoria(categoria)
                .build();
    }

    public Comentario crearComentario(ComentarioRequest req, Tema tema, UsuarioRemoto autor) {
        return Comentario.builder()
                .tema(tema)
                .autor(autor.id(), autor.alias())
                .contenido(req.contenido().trim())
                .build();
    }

    public Comentario crearDatosComentario(ComentarioRequest req) {
        return Comentario.builder().contenido(req.contenido().trim()).build();
    }

    public TemaResponse crearRespuesta(Tema t, long comentarios) {
        return new TemaResponse(t.getId(), t.getTitulo(), t.getContenido(),
                t.getCategoria().getId(), t.getCategoria().getNombre(),
                t.getAutorId(), t.getAutorAlias(), t.getEstado().name(), t.getMotivoBaneo(),
                t.getVistas(), comentarios, t.getFechaCreacion(), t.getFechaActualizacion());
    }

    public ComentarioResponse crearRespuesta(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getTema().getId(), c.getAutorId(), c.getAutorAlias(),
                c.getContenido(), c.getEstado().name(), c.getMotivoBaneo(), c.getFecha());
    }

    public CategoriaResponse crearRespuesta(Categoria c, long temas) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getIcono(), c.getColor(), temas);
    }
}
