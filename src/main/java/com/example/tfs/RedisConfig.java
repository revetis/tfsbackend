package com.example.tfs;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.apps.carts.entities.Cart;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

        // Cart için RedisTemplate
        @Bean
        public RedisTemplate<String, Cart> cartRedisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Cart> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Key serializer
                template.setKeySerializer(new StringRedisSerializer());
                template.setHashKeySerializer(new StringRedisSerializer());

                // Value serializer with Jackson
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                Jackson2JsonRedisSerializer<Cart> serializer = new Jackson2JsonRedisSerializer<>(Cart.class);
                serializer.setObjectMapper(objectMapper);

                template.setValueSerializer(serializer);
                template.setHashValueSerializer(serializer);

                template.afterPropertiesSet();
                return template;
        }

        // Genel StringRedisTemplate
        @Bean
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
                return new StringRedisTemplate(factory);
        }

        // Redis Cache Manager
        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
                RedisSerializer<Object> valueSerializer = RedisSerializer.json();

                Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

                cacheConfigs.put("accessTokenBlacklist",
                                RedisCacheConfiguration.defaultCacheConfig()
                                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                                .fromSerializer(valueSerializer))
                                                .entryTtl(Duration.ofHours(1)));

                cacheConfigs.put("refreshTokenBlacklist",
                                RedisCacheConfiguration.defaultCacheConfig()
                                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                                .fromSerializer(valueSerializer))
                                                .entryTtl(Duration.ofDays(7)));

                cacheConfigs.put("users",
                                RedisCacheConfiguration.defaultCacheConfig()
                                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                                .fromSerializer(valueSerializer))
                                                .entryTtl(Duration.ofMinutes(5)));

                return RedisCacheManager.builder(redisConnectionFactory)
                                .withInitialCacheConfigurations(cacheConfigs)
                                .build();
        }
}
