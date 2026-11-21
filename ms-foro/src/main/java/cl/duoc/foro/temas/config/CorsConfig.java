package cl.duoc.foro.temas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que un frontend en otro origen (por ejemplo Angular en otro puerto o
 * dominio) consuma la API. Los orígenes se configuran con la propiedad
 * {@code app.cors.origenes} (variable de entorno APP_CORS_ORIGENES), separados por coma.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenes;

    public CorsConfig(@Value("${app.cors.origenes:*}") String[] origenes) {
        this.origenes = origenes;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}