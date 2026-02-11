// src/main/java/pharmacie/WebApp.java
package pharmacie;

import org.h2.tools.Server;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.*;

import java.sql.SQLException;

@SpringBootApplication
public class WebApp {

    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }

    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        // démarre un serveur TCP H2 sur le port 9092 et autorise les connexions externes
        // La BD est accessible sur jdbc:h2:tcp://localhost:9092/mem:testdb
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }

    //	@Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/services/commandes/expedier/*")
                    .allowedOrigins("http://localhost:5173");
                registry.addMapping("/services/commandes/ajouterLigne")
                    .allowedOrigins("http://localhost:5173");
            }
        };
    }
}
