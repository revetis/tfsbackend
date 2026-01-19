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

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService implements ITokenService {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    @Transactional
    public Map<String, String> refreshAccessToken(String refreshToken, String accessToken, String ipAddress) {
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

        // Token Linkage Check: Ensure the user being refreshed is the same as the one
        // who owns the current session
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                String refreshSubject = refreshTokenBody.getSubject();
                String accessSubject = null;
                try {
                    Claims accessClaims = Jwts.parserBuilder()
                            .setSigningKey(applicationProperties.getJwtSigningKey())
                            .build()
                            .parseClaimsJws(accessToken)
                            .getBody();
                    accessSubject = accessClaims.getSubject();
                } catch (ExpiredJwtException e) {
                    accessSubject = e.getClaims().getSubject();
                } catch (Exception e) {
                    // Ignore malformed access tokens to allow recovery through refresh if possible
                }

                if (accessSubject != null && !accessSubject.equals(refreshSubject)) {
                    throw new AccessDeniedException("Account mismatch during refresh. Please log in again.");
                }
            } catch (Exception e) {
                if (e instanceof AccessDeniedException)
                    throw e;
            }
        }

        return jwtGenerator.generateAccessToken(refreshToken, ipAddress);
    }
}
