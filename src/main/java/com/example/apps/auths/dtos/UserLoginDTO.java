package com.example.apps.auths.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {

    private String refreshToken;
    private String accessToken;
    private boolean require2fa;
    private String verificationId;
    private String maskedEmail;

}
