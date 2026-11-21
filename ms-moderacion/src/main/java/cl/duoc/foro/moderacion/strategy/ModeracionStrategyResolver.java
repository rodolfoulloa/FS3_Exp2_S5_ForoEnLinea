package cl.duoc.foro.moderacion.strategy;

import cl.duoc.foro.moderacion.model.Enums.TipoContenido;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Elige la {@link ModeracionStrategy} según el tipo de contenido. Spring le
 * entrega todas las estrategias registradas y las indexa por su tipo.
 */
@Component
public class ModeracionStrategyResolver {

    private final Map<TipoContenido, ModeracionStrategy> estrategias = new EnumMap<>(TipoContenido.class);

    public ModeracionStrategyResolver(List<ModeracionStrategy> disponibles) {
        disponibles.forEach(e -> estrategias.put(e.tipo(), e));
    }

    public ModeracionStrategy para(TipoContenido tipo) {
        ModeracionStrategy estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new IllegalStateException("No hay estrategia de moderación para " + tipo);
        }
        return estrategia;
    }
}
