package com.example.apps.orders.repositories;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.apps.orders.entities.Cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepository {

    private final RedisTemplate<String, Cart> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final long DEFAULT_TTL_DAYS = 7;

    private String getKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    public void save(Cart cart) {
        try {
            String key = getKey(cart.getUserId());
            redisTemplate.opsForValue().set(key, cart, DEFAULT_TTL_DAYS, TimeUnit.DAYS);
            log.info("Cart saved for user: {}", cart.getUserId());
        } catch (Exception e) {
            log.error("Error saving cart for user: {}", cart.getUserId(), e);
            throw new RuntimeException("Failed to save cart to Redis", e);
        }
    }

    public Optional<Cart> findByUserId(Long userId) {
        try {
            String key = getKey(userId);
            Cart cart = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(cart);
        } catch (Exception e) {
            log.error("Error retrieving cart for user: {}", userId, e);
            return Optional.empty();
        }
    }

    public void deleteByUserId(Long userId) {
        try {
            String key = getKey(userId);
            redisTemplate.delete(key);
            log.info("Cart deleted for user: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting cart for user: {}", userId, e);
        }
    }

    public void setExpiration(Long userId, long days) {
        try {
            String key = getKey(userId);
            redisTemplate.expire(key, days, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Error setting expiration for cart user: {}", userId, e);
        }
    }
}
