package com.example.apps.auth.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.apps.auths.securities.JWTGenerator;
import com.example.apps.auths.services.impl.TokenService;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    String FAKE_IP_ADDRESS = "127.0.0.1";

    @Mock
    private JWTGenerator jwtGenerator;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void refreshAccessToken_Success() {
        String refreshToken = "validRefreshToken";
        Map<String, String> expectedAccessToken = new HashMap<>();
        expectedAccessToken.put("refreshToken", "newRefreshToken");
        expectedAccessToken.put("accessToken", "newAccessToken");

        when(jwtGenerator.generateAccessToken(refreshToken, anyString())).thenReturn(expectedAccessToken);

        Map<String, String> result = tokenService.refreshAccessToken(refreshToken, anyString());

        assertEquals(expectedAccessToken, result);
        verify(jwtGenerator).generateAccessToken(refreshToken, anyString());
    }
}
