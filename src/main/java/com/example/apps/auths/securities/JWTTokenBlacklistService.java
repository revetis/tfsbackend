package com.example.apps.auths.securities;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class JWTTokenBlacklistService {

    @CachePut(value = "v2_accessTokenBlacklist", key = "#accessToken")
    public Boolean accessTokenBlacklist(String accessToken) {
        return true;
    }

    @CachePut(value = "v2_refreshTokenBlacklist", key = "#refreshToken")
    public Boolean refreshTokenBlacklist(String refreshToken) {
        return true;
    }

    @Cacheable(value = "v2_accessTokenBlacklist", key = "#accessToken")
    public Boolean isAccessTokenBlacklisted(String accessToken) {
        return false;
    }

    @Cacheable(value = "v2_refreshTokenBlacklist", key = "#refreshToken")
    public Boolean isRefreshTokenBlacklisted(String refreshToken) {
        return false;
    }
}
