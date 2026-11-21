package cl.duoc.foro.moderacion.model;

/** Enumeraciones del dominio de moderación. */
public final class Enums {

    private Enums() {
    }

    /** Qué tipo de contenido del foro se reporta o modera. */
    public enum TipoContenido {
        TEMA,
        COMENTARIO
    }

    /** Motivo por el que un usuario reporta contenido. */
    public enum MotivoReporte {
        SPAM,
        OFENSIVO,
        ACOSO,
        CONTENIDO_INAPROPIADO,
        FUERA_DE_TEMA,
        OTRO
    }

    /** Estado del reporte en la bandeja del administrador. */
    public enum EstadoReporte {
        PENDIENTE,
        RESUELTO,
        DESCARTADO
    }

    /** Acción que aplica un administrador sobre el contenido. */
    public enum TipoAccion {
        BANEAR,
        RESTAURAR
    }
}
