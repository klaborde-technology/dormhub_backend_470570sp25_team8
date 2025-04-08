package edu.uscb.csci470sp25.dormhub_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uscb.csci470sp25.dormhub_backend.security.JwtAuthenticationFilter;
import edu.uscb.csci470sp25.dormhub_backend.security.JwtAuthEntryPoint;
import edu.uscb.csci470sp25.dormhub_backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    public SecurityConfig(JwtUtil jwtUtil, JwtAuthEntryPoint jwtAuthEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Publicly accessible from registration and login
                .requestMatchers("/auth/register", "/auth/login").permitAll()
                // User endpoints: view, create, update, delete are only accessible by ADMIN
                .requestMatchers(HttpMethod.GET, "/users").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/user/{id}").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/user").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/user/{id}").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/user/{id}").hasAuthority("ADMIN")
                // Task endpoints: all actions require ADMIN
                .requestMatchers("/task/**").hasAuthority("ADMIN")
                // UserTask endpoints:
                // GET and PUT are accessible by ADMIN GET is accessible by PRIVILEGED_USER
                .requestMatchers(HttpMethod.GET, "/usertask/**").hasAnyAuthority("ADMIN", "PRIVILEGED_USER")
                .requestMatchers(HttpMethod.PUT, "/usertask/**").hasAnyAuthority("ADMIN")
                // POST and DELETE require ADMIN only
                .requestMatchers(HttpMethod.POST, "/usertask/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/usertask/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .accessDeniedHandler(accessDeniedHandler()))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed frontend origins
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "https://multiuserverse-budhrani.netlify.app" // Update URL as needed
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            Map<String, Object> error = new HashMap<>();
            error.put("status", 403);
            error.put("error", "Forbidden");
            error.put("message", "Access denied: Insufficient permissions");
            error.put("path", request.getRequestURI());

            new ObjectMapper().writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}