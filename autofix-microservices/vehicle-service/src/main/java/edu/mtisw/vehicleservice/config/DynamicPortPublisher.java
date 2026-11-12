package edu.mtisw.vehicleservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Publishes the ephemeral Spring port for the Kubernetes ClusterIP sidecar.
 * Eureka continues to register and resolve the actual application port.
 */
@Component
public class DynamicPortPublisher implements ApplicationListener<WebServerInitializedEvent> {
    @Value("${PORT_FILE:}")
    private String portFile;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (portFile == null || portFile.isBlank()) return;
        try {
            Path path = Path.of(portFile);
            Files.createDirectories(path.getParent());
            Files.writeString(path, Integer.toString(event.getWebServer().getPort()),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo publicar el puerto dinámico para Kubernetes", e);
        }
    }
}
