package com.example.apps.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor'ı tüm API yolları için kaydet (* veya /api/** gibi spesifik
        // yollar kullanabilirsiniz)
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
    }
}