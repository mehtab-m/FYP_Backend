package com.scd.fyp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure static resource handler for uploaded files
     * This allows files in uploads/ directory to be served via HTTP
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get the absolute path to uploads directory
        Path uploadsPath = Paths.get("uploads").toAbsolutePath().normalize();
        
        // Register resource handler for uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath.toString() + "/")
                .setCachePeriod(3600);
    }
}
