package cl.duoc.foro.temas.controller;

import cl.duoc.foro.temas.dto.ForoDtos.CategoriaResponse;
import cl.duoc.foro.temas.exception.RecursoNoEncontradoException;
import cl.duoc.foro.temas.factory.ForoFactory;
import cl.duoc.foro.temas.model.Categoria;
import cl.duoc.foro.temas.model.EstadoContenido;
import cl.duoc.foro.temas.repository.CategoriaRepository;
import cl.duoc.foro.temas.repository.TemaRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Categorías del foro con la cantidad de temas activos de cada una. */
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final TemaRepository temaRepository;
    private final ForoFactory factory;

    public CategoriaController(CategoriaRepository categoriaRepository, TemaRepository temaRepository,
                               ForoFactory factory) {
        this.categoriaRepository = categoriaRepository;
        this.temaRepository = temaRepository;
        this.factory = factory;
    }

    /** GET /categorias */
    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(this::respuesta).toList();
    }

    /** GET /categorias/{id} */
    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return respuesta(categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("categoría", id)));
    }

    private CategoriaResponse respuesta(Categoria c) {
        return factory.crearRespuesta(c, temaRepository.countByCategoriaIdAndEstado(c.getId(), EstadoContenido.ACTIVO));
    }
}
