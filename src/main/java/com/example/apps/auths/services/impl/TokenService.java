package com.example.apps.auths.services.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.apps.auths.securities.JWTGenerator;
import com.example.apps.auths.services.ITokenService;

@Service
public class TokenService implements ITokenService {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Override
    public Map<String, String> refreshAccessToken(String refreshToken, String ipAddress) {
        return jwtGenerator.generateAccessToken(refreshToken, ipAddress);

    }

}
