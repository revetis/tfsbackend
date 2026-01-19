package com.example.apps.auths.securities;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class JWTTokenBlacklistService {

    @CachePut(value = "accessTokenBlacklist", key = "#accessToken")
    public Long accessTokenBlacklist(String accessToken) {
        return System.currentTimeMillis();
    }

    @CachePut(value = "refreshTokenBlacklist", key = "#refreshToken")
    public Long refreshTokenBlacklist(String refreshToken) {
        return System.currentTimeMillis();
    }

    @Cacheable(value = "accessTokenBlacklist", key = "#accessToken")
    public Long getAccessTokenBlacklistedTime(String accessToken) {
        return null;
    }

    @Cacheable(value = "refreshTokenBlacklist", key = "#refreshToken")
    public Long getRefreshTokenBlacklistedTime(String refreshToken) {
        return null;
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        return getAccessTokenBlacklistedTime(accessToken) != null;
    }

    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        return getRefreshTokenBlacklistedTime(refreshToken) != null;
    }
}
