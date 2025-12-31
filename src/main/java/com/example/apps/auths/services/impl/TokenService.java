package com.example.apps.auths.services.impl;

import java.util.Map;

import com.example.tfs.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.apps.auths.securities.JWTGenerator;
import com.example.apps.auths.services.ITokenService;

@Service
public class TokenService implements ITokenService {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public Map<String, String> refreshAccessToken(String refreshToken, String ipAddress) {
        Claims refreshTokenBody = Jwts.parserBuilder()
                .setSigningKey(applicationProperties.getJwtSigningKey())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String ipAddressJwt = refreshTokenBody.get("ipAddress", String.class);

        if (ipAddressJwt == null || ipAddress == null || !ipAddressJwt.equals(ipAddress)) {
            throw new AccessDeniedException("IpAddress does not match refresh token");
        }

        String type = refreshTokenBody.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new AccessDeniedException("Invalid token type");
        }

        return jwtGenerator.generateAccessToken(refreshToken, ipAddress);
    }


}
