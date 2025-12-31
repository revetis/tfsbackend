package com.example.tfs.config;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * GlitchTip (Sentry) configuration for error tracking
 */
@Configuration
@Slf4j
public class SentryConfiguration {

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.enabled:true}")
    private boolean sentryEnabled;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${sentry.release:}")
    private String sentryRelease;

    @Value("${sentry.environment:}")
    private String sentryEnvironment;

    @PostConstruct
    public void initSentry() {
        if (!sentryEnabled || sentryDsn == null || sentryDsn.isEmpty()) {
            log.warn("Sentry is disabled or DSN not configured. Error tracking will not work.");
            return;
        }

        try {
            // Initialize Sentry with DSN
            Sentry.init(sentryDsn);

            // Set environment (use sentry.environment if provided, otherwise use Spring profile)
            String environment = sentryEnvironment != null && !sentryEnvironment.isEmpty() 
                    ? sentryEnvironment 
                    : activeProfile;
            Sentry.getContext().addTag("environment", environment);
            Sentry.getContext().addTag("application", "tfs-backend");

            // Set release if provided
            if (sentryRelease != null && !sentryRelease.isEmpty()) {
                Sentry.getContext().addTag("release", sentryRelease);
            }

            log.info("Sentry initialized successfully - DSN: {}, Environment: {}, Release: {}", 
                    sentryDsn.substring(0, Math.min(30, sentryDsn.length())) + "...", 
                    environment, 
                    sentryRelease != null && !sentryRelease.isEmpty() ? sentryRelease : "not set");
        } catch (Exception e) {
            log.error("Failed to initialize Sentry", e);
        }
    }
}

