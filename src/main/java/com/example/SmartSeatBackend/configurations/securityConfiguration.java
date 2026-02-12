package com.example.SmartSeatBackend.configurations;


import com.example.SmartSeatBackend.utility.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class securityConfiguration {


    private final forbiddenHandler myForbiddenHandler;
    private final JwtFilter jFiler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF for JWT/Stateless use
                .csrf(csrf -> csrf.disable())

                // 2. Enable CORS with the bean defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Set session to stateless (we don't want JSESSIONID)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public routes
                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Everything else under /api requires authentication for @PreAuthorize to work
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                // 4. Register your custom JWT Filter before the standard one
                .addFilterBefore(jFiler, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(myForbiddenHandler)
                );

        return http.build();
    }

    // 5. CORS Bean: This is what allows your Angular app to talk to Spring with cookies
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowCredentials(true); // CRITICAL: Allows cookies to be sent
        config.setAllowedOrigins(java.util.List.of("http://localhost:4200")); // Your Angular URL
        config.setAllowedHeaders(java.util.List.of("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}