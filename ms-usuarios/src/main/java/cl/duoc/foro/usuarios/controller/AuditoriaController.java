package cl.duoc.foro.usuarios.controller;

import cl.duoc.foro.usuarios.event.AuditoriaListener;
import cl.duoc.foro.usuarios.event.EventoEntidad;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /auditoria: muestra los últimos eventos registrados por el observador
 * {@link AuditoriaListener}. Permite comprobar el patrón Observer desde Postman.
 */
@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaListener auditoria;

    public AuditoriaController(AuditoriaListener auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    public List<EventoEntidad> ultimosEventos() {
        return auditoria.getUltimosEventos();
    }
}
