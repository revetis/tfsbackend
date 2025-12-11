package com.example.apps.auth.services;

public interface ITokenService {
    public String refreshAccessToken(String refreshToken);

}
