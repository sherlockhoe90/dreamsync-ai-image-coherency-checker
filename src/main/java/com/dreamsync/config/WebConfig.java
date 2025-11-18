package com.dreamsync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${dreamsync.uploads-dir}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files under /uploads/** from the 'uploadsDir' directory on disk
        // Ensure uploadsDir ends without trailing slash, e.g. "./uploads" or "/home/me/uploads"
        String location = "file:" + (uploadsDir.endsWith("/") ? uploadsDir : uploadsDir + "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    // Optional: allow CORS for local testing if UI served from different origin
    // Uncomment if needed:
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**").allowedOrigins("http://localhost:8080").allowedMethods("*");
    // }
}