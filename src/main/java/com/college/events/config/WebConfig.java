package com.college.events.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC wiring: role based access control plus serving uploaded event posters from
 * the {@code uploads/} folder in the working directory.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/student/**", "/organizer/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads").toAbsolutePath();
        String location = uploadDir.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
        try {
            Files.createDirectories(uploadDir);
        } catch (Exception ignored) {
            // The folder is created on first upload if this fails.
        }
    }
}
