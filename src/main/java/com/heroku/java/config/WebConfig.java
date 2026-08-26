package com.heroku.java.config;

import com.heroku.java.service.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves user uploads from the external storage directory (see
 * FileStorageService) under /resources/uploads/** - the URL path templates
 * already use. Static assets bundled inside the jar continue to be served by
 * Spring Boot's default static resource handling.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    public WebConfig(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve from the external storage directory FIRST, then fall back to
        // the bundled assets committed under src/main/resources/static.
        String external = fileStorageService.getUploadDir().toUri().toString();
        registry.addResourceHandler("/resources/uploads/**")
                .addResourceLocations(external, "classpath:/static/resources/uploads/");
    }
}
