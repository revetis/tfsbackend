package com.example.tfs.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom metrics için configuration
 * Prometheus metrics'leri için custom counter, gauge ve timer'lar
 */
@Configuration
public class MetricsConfiguration {

    private final AtomicInteger activeOrders = new AtomicInteger(0);
    private final AtomicInteger totalOrders = new AtomicInteger(0);
    private final AtomicInteger activeUsers = new AtomicInteger(0);

    @Bean
    public Counter orderCreatedCounter(MeterRegistry registry) {
        return Counter.builder("tfs.orders.created")
                .description("Total number of orders created")
                .tag("type", "order")
                .register(registry);
    }

    @Bean
    public Counter orderCompletedCounter(MeterRegistry registry) {
        return Counter.builder("tfs.orders.completed")
                .description("Total number of orders completed")
                .tag("type", "order")
                .register(registry);
    }

    @Bean
    public Counter orderCancelledCounter(MeterRegistry registry) {
        return Counter.builder("tfs.orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("type", "order")
                .register(registry);
    }

    @Bean
    public Counter userRegisteredCounter(MeterRegistry registry) {
        return Counter.builder("tfs.users.registered")
                .description("Total number of users registered")
                .tag("type", "user")
                .register(registry);
    }

    @Bean
    public Timer orderProcessingTimer(MeterRegistry registry) {
        return Timer.builder("tfs.orders.processing.time")
                .description("Time taken to process orders")
                .tag("type", "order")
                .register(registry);
    }

    @Bean
    public Gauge activeOrdersGauge(MeterRegistry registry) {
        return Gauge.builder("tfs.orders.active", activeOrders, AtomicInteger::get)
                .description("Number of active orders")
                .tag("type", "order")
                .register(registry);
    }

    @Bean
    public Gauge totalOrdersGauge(MeterRegistry registry) {
        return Gauge.builder("tfs.orders.total", totalOrders, AtomicInteger::get)
                .description("Total number of orders")
                .tag("type", "order")
                .register(registry);
    }

    @Bean
    public Gauge activeUsersGauge(MeterRegistry registry) {
        return Gauge.builder("tfs.users.active", activeUsers, AtomicInteger::get)
                .description("Number of active users")
                .tag("type", "user")
                .register(registry);
    }

    // Getters for updating metrics
    public AtomicInteger getActiveOrders() {
        return activeOrders;
    }

    public AtomicInteger getTotalOrders() {
        return totalOrders;
    }

    public AtomicInteger getActiveUsers() {
        return activeUsers;
    }
}

