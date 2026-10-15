package edu.mtisw.autofixbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Documentación Swagger (springdoc). Agrega el botón "Authorize" para probar los endpoints con un JWT de Keycloak.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI autofixOpenApi() {
        return new OpenAPI()
                .info(new Info().title("AutoFix API").version("v1")
                        .description("Sistema de Gestión de Reparaciones (HU1-HU7). Autenticación: Bearer JWT emitido por Keycloak."))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
