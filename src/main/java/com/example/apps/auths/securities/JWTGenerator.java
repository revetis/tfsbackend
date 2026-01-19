package com.example.apps.auths.securities;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.apps.auths.entities.User;
import com.example.apps.auths.repositories.IUserRepository;
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
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET_KEY_STR);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
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

    public Map<String, String> generateAccessTokenForLogin(String refreshToken, String ipAddress) {
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
        if (user.getRefreshToken() == null)
            throw new TokenBlacklistException("User Token is null");

        if (!user.getRefreshToken().equals(refreshToken)
                || jwtTokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new TokenBlacklistException("Refresh token is invalid or blacklisted, please log in again");
        }

        List<String> roles = (List<String>) claims.get("roles", List.class);

        // Yeni access token üretimi
        Map<String, String> tokens = new HashMap<>();

        String accessToken = Jwts.builder()
                .setSubject(currentUsername)
                .claim("roles", roles)
                .claim("id", user.getId())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SECRET_KEY)
                .compact();

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
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
        if (user.getRefreshToken() == null)
            throw new TokenBlacklistException("User Token is null");

        List<String> roles = (List<String>) claims.get("roles", List.class);

        if (!user.getRefreshToken().equals(refreshToken)
                || jwtTokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {

            // RACE CONDITION / GRACE PERIOD CHECK
            Long blacklistedAt = jwtTokenBlacklistService.getRefreshTokenBlacklistedTime(refreshToken);
            if (blacklistedAt != null && (System.currentTimeMillis() - blacklistedAt < 30000)) {
                // Within 30 seconds grace period. Return current valid tokens.
                Map<String, String> tokens = new HashMap<>();

                String currentAccessToken = Jwts.builder()
                        .setSubject(currentUsername)
                        .claim("roles", roles)
                        .claim("id", user.getId())
                        .claim("type", "access")
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                        .signWith(SECRET_KEY)
                        .compact();

                tokens.put("accessToken", currentAccessToken);
                tokens.put("refreshToken", user.getRefreshToken());
                return tokens;
            }

            // Actual reuse or invalid token - Security Action: Logout all
            user.setRefreshToken(null);
            userRepository.save(user);
            jwtTokenBlacklistService.refreshTokenBlacklist(refreshToken);
            throw new TokenBlacklistException("Refresh token is invalid or blacklisted, please log in again");
        }

        // Normal rotation
        jwtTokenBlacklistService.refreshTokenBlacklist(refreshToken);

        Map<String, String> tokens = new HashMap<>();

        String accessToken = Jwts.builder()
                .setSubject(currentUsername)
                .claim("roles", roles)
                .claim("id", user.getId())
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

    public String generateGuestActionToken(String orderNumber, String email, String action) {
        return Jwts.builder()
                .setSubject(orderNumber)
                .claim("email", email)
                .claim("action", action)
                .claim("jti", java.util.UUID.randomUUID().toString())
                .claim("type", "guest_action")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000)) // 24 hours
                .signWith(SECRET_KEY)
                .compact();
    }

    public Claims validateGuestToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            if (!"guest_action".equals(type)) {
                throw new TokenBlacklistException("Invalid token type");
            }
            return claims;
        } catch (Exception e) {
            throw new TokenBlacklistException("Invalid or expired guest token");
        }
    }
}
