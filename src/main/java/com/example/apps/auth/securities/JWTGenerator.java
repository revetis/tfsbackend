package com.example.apps.auth.securities;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.settings.ApplicationProperties;
import com.example.settings.exceptions.TokenBlacklistException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTGenerator {

    private final String SECRET_KEY_STR;
    private final SecretKey SECRET_KEY;

    @Autowired
    public JWTGenerator(ApplicationProperties appProperties) {
        this.SECRET_KEY_STR = appProperties.getSECRET_KEY();
        this.SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STR.getBytes());
    }

    @Autowired
    private JWTTokenBlacklistService jwtTokenBlacklistService;

    public String generateRefreshToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles.stream().map(r -> "ROLE_" + r).toList())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String generateAccessToken(String refreshToken) {
        if (jwtTokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new TokenBlacklistException("Refresh token is blacklisted");
        }
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String username = claims.getSubject();
        List<String> roles = (List<String>) claims.get("roles", List.class);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SECRET_KEY)
                .compact();
    }
}
