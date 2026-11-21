package cl.duoc.foro.temas.config;

import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.Comentario;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.model.Tema;
import cl.duoc.foro.temas.repository.CategoriaRepository;
import cl.duoc.foro.temas.repository.ComentarioRepository;
import cl.duoc.foro.temas.repository.TemaRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga las 6 categorías y temas/comentarios de ejemplo si las tablas están
 * vacías (equivale a sql/02_ms_foro.sql). Los autores son los usuarios de
 * prueba de ms-usuarios (IDs 1 a 6).
 */
@Component
@Profile("!test")
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final CategoriaRepository categoriaRepository;
    private final TemaRepository temaRepository;
    private final ComentarioRepository comentarioRepository;

    public DatosIniciales(CategoriaRepository categoriaRepository, TemaRepository temaRepository,
                          ComentarioRepository comentarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.temaRepository = temaRepository;
        this.comentarioRepository = comentarioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }
        Categoria tec = categoriaRepository.save(new Categoria("Tecnología",
                "Programación, hardware, software y todo lo digital", "cpu", "#2F6F5E"));
        Categoria juegos = categoriaRepository.save(new Categoria("Videojuegos",
                "Lanzamientos, consolas, PC gaming y recomendaciones", "controller", "#6C4AB6"));
        Categoria cine = categoriaRepository.save(new Categoria("Cine y Series",
                "Estrenos, críticas, teorías y maratones", "film", "#B5473A"));
        Categoria deportes = categoriaRepository.save(new Categoria("Deportes",
                "Fútbol, tenis, running y deporte en general", "trophy", "#C07A1E"));
        Categoria musica = categoriaRepository.save(new Categoria("Música",
                "Bandas, conciertos, instrumentos y producción musical", "music-note-beamed", "#2E6DB5"));
        Categoria ciencia = categoriaRepository.save(new Categoria("Ciencia",
                "Astronomía, biología, física y divulgación científica", "rocket-takeoff", "#3A8F8A"));

        LocalDateTime ahora = LocalDateTime.now();
        Tema t1 = tema(tec, 3L, "vale.soto", "¿Angular o React para un proyecto nuevo en 2026?",
                "Estoy por empezar un proyecto full stack con Spring Boot en el backend y no me decido por el frontend. "
                        + "¿Qué ventajas le ven a Angular con standalone components frente a React?", ahora.minusDays(6), 128);
        Tema t2 = tema(tec, 6L, "tomas.dev", "Guía rápida: conectar Spring Boot a Oracle Autonomous con wallet",
                "Comparto los pasos que me funcionaron: descargar el wallet, definir TNS_ADMIN y usar el alias _tp "
                        + "en la URL JDBC. Si alguien tiene dudas, pregunte por aquí.", ahora.minusDays(5), 96);
        Tema t3 = tema(juegos, 4L, "mati.gamer", "Los mejores RPG para jugar este verano",
                "Armemos una lista de RPG que valgan la pena. Yo parto con tres: uno de mundo abierto, "
                        + "uno táctico por turnos y uno indie con muy buena historia.", ahora.minusDays(4), 210);
        Tema t4 = tema(juegos, 3L, "vale.soto", "¿Vale la pena armar un PC gamer o comprar consola?",
                "Tengo un presupuesto ajustado y quiero jugar y también programar. "
                        + "¿Qué me recomiendan considerando precio, rendimiento y catálogo?", ahora.minusDays(3), 87);
        Tema t5 = tema(cine, 5L, "fer.cine", "Teorías sobre el final de la temporada que todos comentan",
                "Sin spoilers en el título: dejo mis tres teorías sobre el final y quiero leer las suyas. "
                        + "¿Alguien más notó la pista del primer capítulo?", ahora.minusDays(3), 154);
        Tema t6 = tema(cine, 5L, "fer.cine", "Películas chilenas que todos deberían ver",
                "Hagamos una lista de cine chileno imperdible, desde clásicos hasta estrenos recientes. "
                        + "Indiquen año y por qué la recomiendan.", ahora.minusDays(2), 73);
        Tema t7 = tema(deportes, 4L, "mati.gamer", "Rutina de running para principiantes",
                "Empecé a correr hace un mes y quiero llegar a 10 km. ¿Qué plan semanal me recomiendan "
                        + "para no lesionarme?", ahora.minusDays(2), 45);
        Tema t8 = tema(musica, 6L, "tomas.dev", "¿Qué escuchan mientras programan?",
                "Busco recomendaciones de música para concentrarme: lo-fi, música clásica, bandas sonoras... "
                        + "Compartan sus playlists favoritas.", ahora.minusDays(1), 132);
        Tema t9 = tema(ciencia, 3L, "vale.soto", "Lo que sabemos hoy sobre los exoplanetas habitables",
                "Leí un artículo sobre nuevas detecciones de atmósferas en exoplanetas. "
                        + "¿Qué tan cerca estamos de confirmar uno potencialmente habitable?", ahora.minusHours(20), 64);
        Tema t10 = tema(ciencia, 2L, "diego.mod", "Normas de la categoría Ciencia: fuentes y respeto",
                "Recuerden citar fuentes confiables al compartir datos científicos y mantener un trato respetuoso. "
                        + "Los temas sin fuentes podrán ser moderados.", ahora.minusDays(7), 58);
        Tema spam = tema(tec, 6L, "tomas.dev", "GANA DINERO RÁPIDO DESDE CASA haciendo clic aquí",
                "Oferta increíble, solo por hoy: escribe a este número y gana millones sin hacer nada. "
                        + "Cupos limitados, no te lo pierdas.", ahora.minusDays(1), 12);
        spam.setEstado(EstadoContenido.BANEADO);
        spam.setMotivoBaneo("Spam: publicidad engañosa");
        temaRepository.save(spam);

        comentario(t1, 4L, "mati.gamer", "Angular trae todo integrado (router, formularios, HttpClient). Para equipos grandes se agradece.", ahora.minusDays(6).plusHours(2));
        comentario(t1, 6L, "tomas.dev", "React es más flexible, pero terminas eligiendo muchas librerías. Yo me quedo con Angular.", ahora.minusDays(6).plusHours(5));
        comentario(t1, 5L, "fer.cine", "Desde el punto de vista de alguien que recién parte: la documentación de Angular me pareció muy clara.", ahora.minusDays(5));
        comentario(t2, 3L, "vale.soto", "¡Gracias! Me faltaba el TNS_ADMIN y por eso no conectaba.", ahora.minusDays(5).plusHours(3));
        comentario(t2, 1L, "camila.admin", "Excelente aporte. Lo dejo como referencia para la categoría.", ahora.minusDays(4));
        comentario(t3, 3L, "vale.soto", "Sumo uno de ciencia ficción con decisiones que cambian el final.", ahora.minusDays(4).plusHours(4));
        comentario(t3, 6L, "tomas.dev", "El táctico por turnos es mi favorito, muy buena elección.", ahora.minusDays(3));
        comentario(t4, 4L, "mati.gamer", "Si también vas a programar, el PC gana sin duda.", ahora.minusDays(3).plusHours(2));
        comentario(t5, 3L, "vale.soto", "¡Sí! La pista del reloj en el primer capítulo lo explica todo.", ahora.minusDays(2));
        comentario(t5, 4L, "mati.gamer", "Mi teoría es que todo fue un sueño. No me odien.", ahora.minusDays(2).plusHours(6));
        comentario(t6, 6L, "tomas.dev", "Recomiendo los documentales; hay joyas poco conocidas.", ahora.minusDays(1));
        comentario(t7, 5L, "fer.cine", "Alterna días de trote suave con caminata y sube la distancia de a poco.", ahora.minusDays(1).plusHours(3));
        comentario(t8, 3L, "vale.soto", "Bandas sonoras de videojuegos: están hechas para concentrarse.", ahora.minusHours(18));
        comentario(t8, 4L, "mati.gamer", "Lo-fi siempre. Y nada con letra cuando estoy depurando.", ahora.minusHours(10));
        comentario(t9, 6L, "tomas.dev", "La espectroscopía de los nuevos telescopios está cambiando todo.", ahora.minusHours(8));
        Comentario ofensivo = comentario(t4, 6L, "tomas.dev", "Comentario con insultos hacia otro usuario (contenido moderado).", ahora.minusDays(2));
        ofensivo.setEstado(EstadoContenido.BANEADO);
        ofensivo.setMotivoBaneo("Lenguaje ofensivo hacia otro usuario");
        comentarioRepository.save(ofensivo);

        log.info("Datos iniciales del foro: 6 categorías, {} temas y {} comentarios",
                temaRepository.count(), comentarioRepository.count());
    }

    private Tema tema(Categoria c, Long autorId, String alias, String titulo, String contenido,
                      LocalDateTime fecha, int vistas) {
        return temaRepository.save(Tema.builder().categoria(c).autor(autorId, alias).titulo(titulo)
                .contenido(contenido).fechaCreacion(fecha).vistas(vistas).build());
    }

    private Comentario comentario(Tema t, Long autorId, String alias, String contenido, LocalDateTime fecha) {
        return comentarioRepository.save(Comentario.builder().tema(t).autor(autorId, alias)
                .contenido(contenido).fecha(fecha).build());
    }
}
