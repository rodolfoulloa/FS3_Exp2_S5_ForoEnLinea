package cl.duoc.foro.moderacion.strategy;

import cl.duoc.foro.moderacion.client.ForoClient;
import cl.duoc.foro.moderacion.dto.ModeracionDtos.ContenidoRemoto;
import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import org.springframework.stereotype.Component;

/** Estrategia de moderación para temas. */
@Component
public class TemaModeracionStrategy implements ModeracionStrategy {

    private final ForoClient foroClient;

    public TemaModeracionStrategy(ForoClient foroClient) {
        this.foroClient = foroClient;
    }

    @Override
    public TipoContenido tipo() {
        return TipoContenido.TEMA;
    }

    @Override
    public ContenidoRemoto obtener(Long contenidoId) {
        return foroClient.obtenerTema(contenidoId);
    }

    @Override
    public ContenidoRemoto aplicarEstado(Long contenidoId, String estado, String motivo, Long adminId) {
        return foroClient.moderarTema(contenidoId, estado, motivo, adminId);
    }
}
