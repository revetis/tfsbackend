package com.example.apps.auths.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteTwoFactorToggleRequest {

    @NotBlank(message = "Verification ID is required")
    private String verificationId;

    @NotBlank(message = "Code is required")
    private String code;

    @NotNull(message = "Enable flag is required")
    private Boolean enable;
}
