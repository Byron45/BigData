package com.proyecto.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Este método crea una instancia de RestTemplate que estará disponible
        // para ser inyectada en otros componentes de Spring (como nuestros servicios).
        return new RestTemplate();
    }
}
