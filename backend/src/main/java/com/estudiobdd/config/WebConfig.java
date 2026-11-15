package com.estudiobdd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS liberado somente para a origem da SPA (app.cors.allowed-origin). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String origemPermitida;

    public WebConfig(@Value("${app.cors.allowed-origin}") String origemPermitida) {
        this.origemPermitida = origemPermitida;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origemPermitida)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-Usuario-Id")
                .exposedHeaders("Content-Disposition", "Location");
    }
}
