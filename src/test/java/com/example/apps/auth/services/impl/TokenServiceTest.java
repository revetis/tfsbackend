package com.example.apps.auth.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.apps.auth.securities.JWTGenerator;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private JWTGenerator jwtGenerator;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void refreshAccessToken_Success() {
        String refreshToken = "validRefreshToken";
        String expectedAccessToken = "newAccessToken";

        when(jwtGenerator.generateAccessToken(refreshToken)).thenReturn(expectedAccessToken);

        String result = tokenService.refreshAccessToken(refreshToken);

        assertEquals(expectedAccessToken, result);
        verify(jwtGenerator).generateAccessToken(refreshToken);
    }
}
