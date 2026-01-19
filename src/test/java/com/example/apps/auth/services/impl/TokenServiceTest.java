package com.example.apps.auth.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.apps.auths.securities.JWTGenerator;
import com.example.apps.auths.services.impl.TokenService;
import com.example.tfs.ApplicationProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    String FAKE_IP_ADDRESS = "127.0.0.1";

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private TokenService tokenService;

    private SecretKey secretKey;
    private String validRefreshToken;

    @BeforeEach
    void setUp() {
        // Create a valid secret key for JWT
        secretKey = Keys.hmacShaKeyFor("MyVerySecureSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm".getBytes());

        // Create a valid refresh token
        validRefreshToken = Jwts.builder()
                .setSubject("testuser")
                .claim("ipAddress", FAKE_IP_ADDRESS)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(secretKey)
                .compact();
    }

    @Test
    void refreshAccessToken_Success() {
        Map<String, String> expectedAccessToken = new HashMap<>();
        expectedAccessToken.put("refreshToken", "newRefreshToken");
        expectedAccessToken.put("accessToken", "newAccessToken");

        when(applicationProperties.getJwtSigningKey()).thenReturn(secretKey);
        when(jwtGenerator.generateAccessToken(validRefreshToken, FAKE_IP_ADDRESS)).thenReturn(expectedAccessToken);

        // passing null as accessToken is fine (optional)
        Map<String, String> result = tokenService.refreshAccessToken(validRefreshToken, null, FAKE_IP_ADDRESS);

        assertEquals(expectedAccessToken, result);
        verify(jwtGenerator).generateAccessToken(validRefreshToken, FAKE_IP_ADDRESS);
    }

    @Test
    void refreshAccessToken_Failure_UserMismatch() {
        // Create an access token for a DIFFERENT user
        String mismatchedAccessToken = Jwts.builder()
                .setSubject("otheruser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();

        when(applicationProperties.getJwtSigningKey()).thenReturn(secretKey);

        assertThrows(AccessDeniedException.class, () -> {
            tokenService.refreshAccessToken(validRefreshToken, mismatchedAccessToken, FAKE_IP_ADDRESS);
        });

        verify(jwtGenerator, never()).generateAccessToken(anyString(), anyString());
    }
}
