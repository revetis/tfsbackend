package com.example.tfs;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

        // Genel StringRedisTemplate
        @Bean
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
                return new StringRedisTemplate(factory);
        }

        // Redis Cache Manager
        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
                RedisSerializer<Object> valueSerializer = RedisSerializer.json();

                // Define DEFAULT configuration for ALL caches
                RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(valueSerializer))
                                .entryTtl(Duration.ofHours(1));

                Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

                cacheConfigs.put("accessTokenBlacklist", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
                cacheConfigs.put("refreshTokenBlacklist", defaultCacheConfig.entryTtl(Duration.ofDays(7)));
                cacheConfigs.put("users", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
                cacheConfigs.put("activeCart", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
                cacheConfigs.put("cartCheckoutCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(defaultCacheConfig) // Set the default here
                                .withInitialCacheConfigurations(cacheConfigs)
                                .build();
        }
}
