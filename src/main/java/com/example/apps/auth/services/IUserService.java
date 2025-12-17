package com.example.apps.auth.services;

import java.security.Principal;

import org.springframework.web.multipart.MultipartFile;

import com.example.apps.auth.dtos.ForgotPasswordDTOIU;
import com.example.apps.auth.dtos.ForgotPasswordSetNewPasswordDTOIU;
import com.example.apps.auth.dtos.ResetPasswordDTOIU;
import com.example.apps.auth.dtos.UserDTO;
import com.example.apps.auth.dtos.UserDTOCreate;
import com.example.apps.auth.dtos.UserDTOUpdate;
import com.example.apps.auth.dtos.UserLoginDTO;
import com.example.apps.auth.dtos.UserLoginDTOIU;
import com.example.apps.auth.dtos.UserRegisterDTO;
import com.example.apps.auth.dtos.UserRegisterDTOIU;
import com.example.apps.auth.dtos.UserUpdateDTOIU;

import java.util.List;

public interface IUserService {
    public UserRegisterDTO registerUser(UserRegisterDTOIU request);

    public UserLoginDTO login(UserLoginDTOIU request, String ipAddress);

    public String logout(String accessToken, String refreshToken);

    public UserDTO profile(String username, Long userId);

    public UserDTO updateProfile(UserUpdateDTOIU request, Principal principal);

    public void avatar(MultipartFile file, Long userId);

    public void resetPassword(ResetPasswordDTOIU request, Principal principal);

    public void forgotPassword(ForgotPasswordDTOIU request, ForgotPasswordSetNewPasswordDTOIU newPassword,
            String... args);

    public void verifyEmail(String token);

    public void sendVerifyEmail(Principal principal);

    // Admin methods
    public List<UserDTO> getAllUsers();

    public UserDTO getUserById(Long userId);

    public void deleteUser(Long userId);

    public UserDTO updateUser(Long userId, UserDTOUpdate request);

    public UserDTO createUser(UserDTOCreate request);

    public void putUserRole(Long userId, Long roleId);

    public void deleteUserRole(Long userId, Long roleId);

    public void enableUser(Long userId);

    public void disableUser(Long userId);

}
