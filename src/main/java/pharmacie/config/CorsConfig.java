package pharmacie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS globale pour toute l'application
 * Autorise les requêtes cross-origin depuis n'importe quelle origine
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configuration CORS au niveau du WebMVC
     * Cette configuration s'applique à tous les contrôleurs REST
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**") // Tous les endpoints
                .allowedOriginPatterns("*") // Toutes les origines (avec credentials possible)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD") // Toutes les méthodes HTTP
                .allowedHeaders("*") // Tous les headers
                .allowCredentials(true) // Autoriser les cookies/credentials
                .maxAge(3600); // Cache de la réponse preflight pendant 1 heure
    }

    /**
     * Filtre CORS global pour toute l'application
     * Cette configuration s'applique également à Spring Data REST et autres endpoints
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Autoriser les credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Autoriser toutes les origines avec pattern
        config.addAllowedOriginPattern("*");

        // Autoriser tous les headers
        config.addAllowedHeader("*");

        // Autoriser toutes les méthodes HTTP
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");

        // Exposer tous les headers dans la réponse
        config.addExposedHeader("*");

        // Cache de la réponse preflight
        config.setMaxAge(3600L);

        // Appliquer cette configuration à tous les endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
