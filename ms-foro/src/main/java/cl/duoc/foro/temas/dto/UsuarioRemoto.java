package cl.duoc.foro.temas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Datos del usuario que entrega ms-usuarios (solo los que necesita ms-foro). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioRemoto(Long id, String alias, String rol, Boolean activo) {

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }
}
