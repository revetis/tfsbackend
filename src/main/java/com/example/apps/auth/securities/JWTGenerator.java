package com.example.apps.auth.securities;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.apps.auth.entities.User;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.tfs.ApplicationProperties;
import com.example.tfs.exceptions.TokenBlacklistException;
import com.example.tfs.exceptions.UserNotFoundException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTGenerator {

    private final String SECRET_KEY_STR;
    private final SecretKey SECRET_KEY;

    public JWTGenerator(ApplicationProperties appProperties) {
        this.SECRET_KEY_STR = appProperties.getSECRET_KEY();
        this.SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STR.getBytes());
    }

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JWTTokenBlacklistService jwtTokenBlacklistService;

    public String generateRefreshToken(String username, List<String> roles, String ipAddress) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles.stream().map(r -> "ROLE_" + r).toList())
                .claim("type", "refresh")
                .claim("ipAddress", ipAddress)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Map<String, String> generateAccessToken(String refreshToken, String ipAddress) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (Exception e) {
            throw new TokenBlacklistException("Refresh token is invalid or malformed");
        }

        String currentUsername = claims.getSubject();
        String tokenType = claims.get("type", String.class);
        String tokenIp = claims.get("ipAddress", String.class);

        if (!"refresh".equals(tokenType)) {
            throw new TokenBlacklistException("Invalid token type for this endpoint");
        }
        if (tokenIp == null || tokenIp.isBlank() || !tokenIp.equals(ipAddress)) {
            throw new TokenBlacklistException("This token is not valid for this IP address");
        }

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Log in first"));

        if (!user.getRefreshToken().equals(refreshToken)
                || jwtTokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            jwtTokenBlacklistService.refreshTokenBlacklist(refreshToken);
            throw new TokenBlacklistException("Refresh token is invalid or blacklisted, please log in again");
        }

        List<String> roles = (List<String>) claims.get("roles", List.class);

        // Eski refresh token blacklist'e ekleniyor
        jwtTokenBlacklistService.refreshTokenBlacklist(refreshToken);

        // Yeni access ve refresh token üretimi
        Map<String, String> tokens = new HashMap<>();

        String accessToken = Jwts.builder()
                .setSubject(currentUsername)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SECRET_KEY)
                .compact();

        String newRefreshToken = Jwts.builder()
                .setSubject(currentUsername)
                .claim("roles", roles)
                .claim("type", "refresh")
                .claim("ipAddress", ipAddress)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(SECRET_KEY)
                .compact();

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", newRefreshToken);

        return tokens;
    }
}
