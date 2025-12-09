package com.example.apps.auth.security;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.settings.ApplicationProperties;

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

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles.stream().map(r -> "ROLE_" + r).toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SECRET_KEY)
                .compact();
    }
}
