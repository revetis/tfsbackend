package com.example.apps.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiterService;

    // Interceptor'ın asıl çalıştığı metot
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        final String CLIENT_KEY = request.getRemoteAddr();

        // 1. İlgili Key'e ait kovayı al
        Bucket bucket = rateLimiterService.resolveBucket(CLIENT_KEY);

        // 2. Kovadan 1 token tüketmeyi dene
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Tüketim başarılı: İsteğe devam et

            // Opsiyonel: Kalan token sayısını HTTP başlığı olarak dönmek iyi bir
            // uygulamadır.
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true; // Controller'a gitmeye izin ver

        } else {
            // Tüketim başarısız: Rate limit aşıldı

            // HTTP 429 (Too Many Requests) yanıt kodu dön
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            // İstemcinin kaç saniye beklemesi gerektiğini HTTP başlığı olarak dön
            long secondsToWait = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("Retry-After", String.valueOf(secondsToWait));

            response.getWriter()
                    .write("Rate limit exceeded. Please try again in" + secondsToWait + " seconds.");
            return false;
        }
    }
}