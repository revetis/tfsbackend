package com.example.apps.auths.securities;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {

        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        return Bucket.builder()

                .addLimit(limit -> limit.capacity(1000).refillGreedy(1000, Duration.ofMinutes(1)))
                .build();
    }
}