package pharmacie.config;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApplicationStartupLogger implements ApplicationListener<WebServerInitializedEvent> {
    @SuppressWarnings("null")
    @Override
    public void onApplicationEvent(@NonNull WebServerInitializedEvent event) {
        int serverPort = event.getWebServer().getPort();
        WebApplicationContext webApplicationContext = (WebApplicationContext) event.getApplicationContext();
        String contextPath = webApplicationContext.getServletContext().getContextPath();
        String appUrl = "http://localhost:" + serverPort + contextPath;
        log.info("L'application est démarrée sur : {}", appUrl);
    }
}
