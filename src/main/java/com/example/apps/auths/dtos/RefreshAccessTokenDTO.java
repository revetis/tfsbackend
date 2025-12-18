package com.example.apps.auths.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshAccessTokenDTO {
    private String refreshToken;
    private String accessToken;
}
