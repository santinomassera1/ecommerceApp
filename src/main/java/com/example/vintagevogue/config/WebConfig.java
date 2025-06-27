package com.example.vintagevogue.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Spring Boot maneja automáticamente los recursos estáticos desde classpath:/static/
        // Solo necesitamos agregar la configuración para el directorio físico donde se guardan las nuevas imágenes
        String projectPath = System.getProperty("user.dir");
        String imageDirectory = "file:" + projectPath + "/src/main/resources/static/images/";
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/", imageDirectory);
    }
}