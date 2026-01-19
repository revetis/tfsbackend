package com.example.apps.auths.securities;

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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Identify sensitive paths: Auth POSTs and Coupon Validation
        boolean isSensitive = (requestURI.startsWith("/rest/api/public/auth/") && "POST".equalsIgnoreCase(method))
                || requestURI.equals("/rest/api/public/campaigns/coupons/validate");

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0].trim();
        }
        final String CLIENT_KEY = clientIp;

        Bucket bucket = rateLimiterService.resolveBucket(CLIENT_KEY, isSensitive);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            long secondsToWait = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("Retry-After", String.valueOf(secondsToWait));
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"message\": \"Too many requests. Please try again in " + secondsToWait + " seconds.\"}");
            return false;
        }
    }
}