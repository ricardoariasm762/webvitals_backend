package com.edu.vitals.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC CORS configuration binding externalized properties with fallback.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public CorsConfig(@Autowired(required = false) CorsProperties corsProperties) {
        this.corsProperties = corsProperties != null ? corsProperties : new CorsProperties();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = corsProperties.getAllowedOrigins().toArray(new String[0]);
        String[] methods = corsProperties.getAllowedMethods().toArray(new String[0]);
        String[] headers = corsProperties.getAllowedHeaders().toArray(new String[0]);

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods(methods)
                .allowedHeaders(headers)
                .allowCredentials(corsProperties.isAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }
}
