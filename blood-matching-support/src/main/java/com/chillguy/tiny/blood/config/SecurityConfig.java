package com.chillguy.tiny.blood.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public resources and pages
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/", "/index", "/home", "/about",
                                "/auth/**", // Login, register pages
                                "/profile", // Profile page template
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()
                        
                        // Public API endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/blood-requests/confirm-by-token").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF", "ROLE_MEMBER")
                        
                        // Template pages - allow access, JavaScript will handle auth check
                        .requestMatchers("/blood-requests/**").permitAll()
                        
                        // Protected API endpoints - require JWT authentication  
                        .requestMatchers(HttpMethod.GET, "/api/blood-requests/getall").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/blood-requests/update-status/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/blood-requests/process").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/blood-requests/create").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF", "ROLE_MEMBER")
                        .requestMatchers(HttpMethod.GET, "/api/blood-requests/my-requests").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF", "ROLE_MEMBER")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/auth/reset-password").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Handle exceptions for unauthorized access
                .exceptionHandling(exceptions -> 
                    exceptions.authenticationEntryPoint((request, response, authException) -> {
                        // For API requests, return 401
                        if (request.getRequestURI().startsWith("/api/")) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication token is required\"}");
                        } else {
                            // For page requests, redirect to login
                            response.sendRedirect("/auth/login");
                        }
                    })
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "https://swp391-su25-1.onrender.com"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type", "Authorization", "X-Requested-With", "Origin", "Accept"
        ));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }




}
