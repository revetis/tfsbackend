package com.example.apps.auths.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshAccessTokenDTOIU {
    @NotBlank
    private String refreshToken;
}
