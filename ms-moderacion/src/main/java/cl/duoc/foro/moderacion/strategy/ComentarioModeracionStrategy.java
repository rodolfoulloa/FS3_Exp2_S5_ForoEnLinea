package cl.duoc.foro.moderacion.strategy;

import cl.duoc.foro.moderacion.client.ForoClient;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import org.springframework.stereotype.Component;

/** Estrategia de moderación para comentarios. */
@Component
public class ComentarioModeracionStrategy implements ModeracionStrategy {

    private final ForoClient foroClient;

    public ComentarioModeracionStrategy(ForoClient foroClient) {
        this.foroClient = foroClient;
    }

    @Override
    public TipoContenido tipo() {
        return TipoContenido.COMENTARIO;
    }

    @Override
    public ContenidoRemoto obtener(Long contenidoId) {
        return foroClient.obtenerComentario(contenidoId);
    }

    @Override
    public ContenidoRemoto aplicarEstado(Long contenidoId, String estado, String motivo, Long adminId) {
        return foroClient.moderarComentario(contenidoId, estado, motivo, adminId);
    }
}
