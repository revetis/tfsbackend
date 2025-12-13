package com.example.apps.auth.securities;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class JWTTokenBlacklistService {

    @CachePut(value = "accessTokenBlacklist", key = "#accessToken")
    public String accessTokenBlacklist(String accessToken) {
        return accessToken;
    }

    @CachePut(value = "refreshTokenBlacklist", key = "#refreshToken")
    public String refreshTokenBlacklist(String refreshToken) {
        return refreshToken;
    }

    @Cacheable(value = "accessTokenBlacklist", key = "#accessToken")
    public boolean isAccessTokenBlacklisted(String accessToken) {
        return false;
    }

    @Cacheable(value = "refreshTokenBlacklist", key = "#refreshToken")
    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        return false;
    }
}
