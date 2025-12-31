package com.example.apps.auths.securities;

import com.example.tfs.ApplicationProperties;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

        @Autowired
        private JWTFilter jwtFilter;

        @Autowired
        private ApplicationProperties applicationProperties;

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
                        "/line-awesome/**", // Vaadin ikonları için
                        "/uploads/**", // Statik upload dosyaları
                        "/img/**" // Yeni eklenen görsel yolu

        };

        @Bean
        public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring().requestMatchers("/uploads/**", "/img/**", "/favicon.ico", "/error",
                                "/rest/api/public/payments/callback/**", "/rest/api/public/webhook/geliver/**");
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(applicationProperties.getFRONTEND_URL(),
                                "http://localhost:5173", "http://localhost:5174", "http://localhost:3000"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cookie"));
                configuration.setAllowCredentials(true);

                // Özel Callback CORS Ayarı (Sadece Iyzico ve null origin)
                CorsConfiguration callbackConfig = new CorsConfiguration();
                // "null" origin bazen redirect/form post durumlarında gerekir.
                callbackConfig.setAllowedOriginPatterns(
                                Arrays.asList("https://*.iyzipay.com", "https://*.iyzico.com", "null"));
                callbackConfig.setAllowedMethods(Arrays.asList("POST", "GET", "OPTIONS"));
                callbackConfig.setAllowedHeaders(Arrays.asList("*"));
                callbackConfig.setAllowCredentials(false);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/rest/api/public/payments/callback/**", callbackConfig);
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {

                boolean debugMode = Boolean.TRUE.equals(applicationProperties.getDEBUG_MODE());

                http.csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // Security Headers
                                .headers(headers -> {
                                        if (!debugMode) {
                                                headers.contentTypeOptions(contentTypeOptions -> {
                                                });
                                        }
                                        headers.frameOptions(frameOptions -> {
                                                if (debugMode) {
                                                        frameOptions.disable();
                                                } else {
                                                        frameOptions.deny();
                                                }
                                        });
                                        headers.xssProtection(xssProtection -> xssProtection
                                                        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK));
                                        headers.referrerPolicy(referrerPolicy -> referrerPolicy
                                                        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                                        if (!debugMode) {
                                                headers.httpStrictTransportSecurity(hsts -> hsts
                                                                .maxAgeInSeconds(31536000)
                                                                .includeSubDomains(true));
                                        }
                                })
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/rest/api/public/auth/**").permitAll()
                                                .requestMatchers("/rest/api/public/**").permitAll()

                                                .requestMatchers(PUBLIC_URLS).permitAll()

                                                .requestMatchers("/rest/api/private/**")
                                                .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                                                .requestMatchers("/rest/api/admin/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(ex -> ex
                                                .accessDeniedHandler(accessDeniedHandler(objectMapper))
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        var body = ApiTemplate.apiTemplateGenerator(
                                                                        false, 401, request.getRequestURI(),
                                                                        ApiErrorTemplate.apiErrorTemplateGenerator(
                                                                                        false, 401,
                                                                                        request.getRequestURI(),
                                                                                        "Unauthorized: " + authException
                                                                                                        .getMessage()),
                                                                        null);
                                                        response.getWriter()
                                                                        .write(objectMapper.writeValueAsString(body));
                                                }))
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
        AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
