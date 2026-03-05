package com.example.SmartSeatBackend.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class corsConfig {

    // 1. Define the Configuration as a Bean so Security can find it
    @Bean
    public CorsConfiguration corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        // Add your Vercel and Localhost origins
        config.addAllowedOrigin("https://smart-seat-frontend-three.vercel.app/");
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        config.addExposedHeader("Set-Cookie");

        return config;
    }

    // 2. Use the bean above to create the Filter
    @Bean
    public CorsFilter corsFilter(CorsConfiguration corsConfiguration) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}