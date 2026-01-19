package com.example.apps.auths.services;

import java.security.Principal;

import com.example.apps.auths.dtos.*;
import org.springframework.web.multipart.MultipartFile;

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

        public UserDTO verifyEmail(String token);

        public void sendVerifyEmail(Principal principal);

        // Admin methods
        public List<UserDTO> getAllUsers();

        public UserDTO getUserById(Long userId);

        public UserDTO getUserByUsernameOrEmail(String usernameOrEmail);

        public void deleteUser(Long userId);

        public UserDTO updateUser(Long userId, UserDTOUpdate request);

        public UserDTO createUser(UserDTOCreate request);

        public void putUserRole(Long userId, Long roleId);

        public void deleteUserRole(Long userId, Long roleId);

        public void enableUser(Long userId);

        public void disableUser(Long userId);

        public AccessCheckDTO accessCheck(String refreshToken, String accessToken, String ipAddress);

        void adminResetPassword(Long userId, String newPassword);

        public com.example.apps.auths.entities.User getById(Long id);

        UserPageResult getAllUsers(int start, int end, String sortField, String sortOrder, String search,
                        String username, String email, Long roleId);

        void deleteMyAccount(DeleteAccountRequest request, Principal principal);

        UserLoginDTO verifyTwoFactorLogin(String verificationId, String code, String ipAddress);

        String initiateTwoFactorToggle(Long userId);

        void completeTwoFactorToggle(Long userId, String verificationId, String code, Boolean enable);

        // Result record for paginated users
        record UserPageResult(List<UserDTO> data, long totalCount) {
        }
}
