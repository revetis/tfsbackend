package com.example.apps.auth.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.apps.auth.security.JWTGenerator;
import com.example.apps.auth.services.ITokenService;

@Service
public class TokenService implements ITokenService {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Override
    public String refreshAccessToken(String refreshToken) {
        return jwtGenerator.generateAccessToken(refreshToken);

    }

}
