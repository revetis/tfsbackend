package com.example.apps.auth.services;

import java.util.Map;

public interface ITokenService {
    public Map<String, String> refreshAccessToken(String refreshToken, String ipAddress);

}
