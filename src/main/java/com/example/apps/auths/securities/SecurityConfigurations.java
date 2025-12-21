package com.example.apps.auths.securities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.tfs.maindto.ApiErrorTemplate;
import com.example.tfs.maindto.ApiTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfigurations {

        @Autowired
        private JWTFilter jwtFilter;

        private static final String[] PUBLIC_URLS = {
                        "/favicon.ico",
                        "/redoc.html", // Redoc HTML sayfası
                        "/openapi.yaml", // OpenAPI tanım dosyası
                        "/v3/api-docs/**", // Genellikle diğer Swagger/OpenAPI yolları
                        "/swagger-ui.html", // Swagger UI sayfası
                        "/swagger-ui/**", // Swagger UI statik kaynakları
                        "/.well-known/**", // Tarayıcı uzantıları gibi özel yollar
                        "/VAADIN/**", // Vaadin'in statik dosyaları
                        "/admin/**", // Sizin panel yolunuz
                        "/line-awesome/**" // Vaadin ikonları için
        };

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {

                http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(
                                                auth -> auth.requestMatchers(PUBLIC_URLS).permitAll()

                                                                .requestMatchers("/rest/api/public/**", "/error")
                                                                .permitAll()
                                                                .requestMatchers("/rest/api/private/**").hasRole("USER")
                                                                .requestMatchers("/rest/api/admin/**").hasRole("ADMIN")
                                                                .anyRequest().denyAll())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(ex -> ex
                                                .accessDeniedHandler(accessDeniedHandler(objectMapper)))
                                .logout(l -> l.logoutUrl("/logout").permitAll());

                return http.build();
        }

        @Bean
        AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
                return (HttpServletRequest request, HttpServletResponse response,
                                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        var body = ApiTemplate.apiTemplateGenerator(
                                        false, 403, request.getRequestURI(),
                                        ApiErrorTemplate.apiErrorTemplateGenerator(false, 403, request.getRequestURI(),
                                                        accessDeniedException.getMessage()),
                                        null);
                        response.getWriter().write(objectMapper.writeValueAsString(body));
                };
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
                return config.getAuthenticationManager();
        }
}
