package pharmacie.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Type;

import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Component
public class SpringDataRestConfig
        implements RepositoryRestConfigurer {
    // @Autowired
    private final EntityManager entityManager;

    public SpringDataRestConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config, CorsRegistry cors) {
        // Expose les id de toutes les entités dans l'API REST
        config.exposeIdsFor(
            entityManager.getMetamodel()
                        .getEntities()
                        .stream()
                        .map(Type::getJavaType)
                        .toArray(Class[]::new));

        // Autorise les requêtes CORS pour Spring Data REST
        // Note: Une configuration CORS globale existe aussi dans CorsConfig.java
        cors.addMapping("/**") // Toutes les mappings sont autorisées
                .allowedOriginPatterns("*") // Toutes les origines sont autorisées (avec credentials)
                .allowedMethods("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS", "HEAD") // Toutes les méthodes HTTP
                .allowedHeaders("*") // Tous les headers
                .exposedHeaders("*") // Exposer tous les headers de réponse
                .allowCredentials(true) // Autoriser les cookies/credentials
                .maxAge(3600); // Durée de la réponse preflight en secondes

    }

}
