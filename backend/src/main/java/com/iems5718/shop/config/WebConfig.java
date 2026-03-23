package com.iems5718.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-CSRF-Token")
                .allowCredentials(true)
                .maxAge(3600);
        registry.addMapping("/images/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET")
                .maxAge(86400);
        registry.addMapping("/videos/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET")
                .maxAge(86400);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:../images/");
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:../videos/");
    }
}
