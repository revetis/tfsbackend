package com.example.apps.auth.services;

import java.security.Principal;

import org.springframework.web.multipart.MultipartFile;

import com.example.apps.auth.dto.ForgotPasswordDTOIU;
import com.example.apps.auth.dto.ForgotPasswordSetNewPasswordDTOIU;
import com.example.apps.auth.dto.ResetPasswordDTOIU;
import com.example.apps.auth.dto.UserDTO;
import com.example.apps.auth.dto.UserLoginDTO;
import com.example.apps.auth.dto.UserLoginDTOIU;
import com.example.apps.auth.dto.UserRegisterDTO;
import com.example.apps.auth.dto.UserRegisterDTOIU;
import com.example.apps.auth.dto.UserUpdateDTOIU;

public interface IUserService {
    public UserRegisterDTO registerUser(UserRegisterDTOIU request);

    public UserLoginDTO login(UserLoginDTOIU request);

    public String logout(String accessToken, String refreshToken);

    public UserDTO profile(String username);

    public UserDTO updateProfile(UserUpdateDTOIU request, Principal principal);

    public void avatar(MultipartFile file, Principal principal);

    public void resetPassword(ResetPasswordDTOIU request, Principal principal);

    public void forgotPassword(ForgotPasswordDTOIU request, ForgotPasswordSetNewPasswordDTOIU newPassword,
            String... args);

    public void verifyEmail(String token);

}
