package com.example.apps.auths.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResetPasswordDTOIU {
    @NotBlank(message = "New password is required")
    private String newPassword;
}
