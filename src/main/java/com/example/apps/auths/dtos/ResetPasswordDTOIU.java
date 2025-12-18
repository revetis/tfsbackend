package com.example.apps.auths.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTOIU {

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String currentPassword;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String newPassword;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String newPasswordRetry;

}
