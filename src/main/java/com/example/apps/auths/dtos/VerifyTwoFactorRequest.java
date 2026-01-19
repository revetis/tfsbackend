package com.example.apps.auths.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyTwoFactorRequest {

    @NotBlank(message = "Verification ID is required")
    private String verificationId;

    @NotBlank(message = "Code is required")
    private String code;
}
