package com.example.apps.auths.securities;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, boolean isSensitive) {
        String cacheKey = isSensitive ? key + "_SENSITIVE" : key;
        return cache.computeIfAbsent(cacheKey, k -> createNewBucket(isSensitive));
    }

    public Bucket resolveBucket(String key) {
        return resolveBucket(key, false);
    }

    private Bucket createNewBucket(boolean isSensitive) {
        if (isSensitive) {
            // Strict limit for sensitive endpoints: 10 requests per minute
            return Bucket.builder()
                    .addLimit(limit -> limit.capacity(10).refillGreedy(10, Duration.ofMinutes(1)))
                    .build();
        }
        // General limit for other requests: 1000 requests per minute
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(1000).refillGreedy(1000, Duration.ofMinutes(1)))
                .build();
    }
}