package com.example.apps.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequestDTOIU {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    @NotBlank(message = "Access token is required")
    private String accessToken;
}
