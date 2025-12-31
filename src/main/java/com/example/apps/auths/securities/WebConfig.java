package com.example.apps.auths.securities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.tfs.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String staticPath = applicationProperties.getSTATIC_PATH();
        String absolutePath = Paths.get(staticPath).toAbsolutePath().toUri().toString();

        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }

        log.info("Mapping /img/** and /uploads/** to: {}", absolutePath);

        // Serve everything under /img/** from the root of static folder
        registry.addResourceHandler("/img/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(0);

        // Serve all uploads from static folder
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath + "uploads/")
                .setCachePeriod(0);
    }
}