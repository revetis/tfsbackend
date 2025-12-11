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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        final String CLIENT_KEY = request.getRemoteAddr();

        Bucket bucket = rateLimiterService.resolveBucket(CLIENT_KEY);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true; // Controller'a gitmeye izin ver

        } else {

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            long secondsToWait = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("Retry-After", String.valueOf(secondsToWait));

            response.getWriter()
                    .write("Rate limit exceeded. Please try again in" + secondsToWait + " seconds.");
            return false;
        }
    }
}