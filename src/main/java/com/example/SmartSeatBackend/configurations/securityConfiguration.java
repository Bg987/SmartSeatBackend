package com.example.SmartSeatBackend.configurations;


import com.example.SmartSeatBackend.utility.JwtFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class securityConfiguration {

    private final forbiddenHandler myForbiddenHandler;
    private final JwtFilter jFiler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for JWT
                // Link directly to the bean defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/v3/api-docs/**", "/swagger-ui/**","/actuator/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                )

                .addFilterBefore(jFiler, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.accessDeniedHandler(myForbiddenHandler));

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

        config.setAllowCredentials(true);

        config.setAllowedOrigins(java.util.List.of(
                "http://localhost:4200",
                "https://smart-seat-frontend-three.vercel.app",
                "https://exam-portal-smart-seat-frontend.vercel.app",
                "https://proxy-0xaq.onrender.com" // ADD YOUR PROXY HERE
        ));

        // Add X-Requested-With and the SEB header to allowed headers
        config.setAllowedHeaders(java.util.List.of(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "X-SafeExamBrowser-ConfigKeyhash"
        ));

        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Ensure the browser can see these headers in the response
        config.setExposedHeaders(java.util.List.of("Set-Cookie", "Authorization"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}