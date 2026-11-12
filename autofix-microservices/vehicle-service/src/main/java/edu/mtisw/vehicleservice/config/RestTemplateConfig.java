package edu.mtisw.vehicleservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// @LoadBalanced: las URLs usan el nombre del microservicio (http://repair-service/...) y Eureka
// entrega la IP y el puerto (asignado dinámicamente) de una instancia disponible.
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
