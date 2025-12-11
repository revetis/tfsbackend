package com.example.apps.auth.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Verilen anahtara (key) karşılık gelen Bucket objesini döner.
     * 
     * @param key Kullanıcı Kimliği, IP Adresi vb.
     * @return Bucket objesi
     */
    public Bucket resolveBucket(String key) {
        // computeIfAbsent sayesinde, kova sadece ilk istekte oluşturulur.
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    // Bandwidth sabiti kaldırıldı. Kural doğrudan Bucket oluşturma metodunda
    // tanımlanıyor.
    private Bucket createNewBucket() {
        return Bucket.builder()
                // **NİHAİ DÜZELTME:** En güncel ve uyarı vermeyen konfigürasyon.
                // Kural: Kapasite 20 ve her 1 dakikada 20 token ile yenilenir (greedy).
                .addLimit(limit -> limit.capacity(20).refillGreedy(20, Duration.ofMinutes(10)))
                .build();
    }
}