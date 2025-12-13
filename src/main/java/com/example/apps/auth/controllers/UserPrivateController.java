package com.example.apps.auth.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.apps.auth.dtos.LogoutRequestDTOIU;
import com.example.apps.auth.dtos.ResetPasswordDTOIU;
import com.example.apps.auth.dtos.UserDTO;
import com.example.apps.auth.dtos.UserUpdateDTOIU;
import com.example.apps.auth.services.IUserService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/private/auth")
public class UserPrivateController {

    @Autowired
    private IUserService userService;

    @GetMapping(path = "/profile/{username}")
    public UserDTO profile(@PathVariable String username) {
        return userService.profile(username);
    }

    @PostMapping(path = "/profile/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        userService.avatar(file, principal);
        return ResponseEntity.ok("Avatar uploaded successfully");
    }

    @PutMapping(path = "/profile")
    public UserDTO updateProfile(@RequestBody @Valid UserUpdateDTOIU request, Principal principal) {
        return userService.updateProfile(request, principal);
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid LogoutRequestDTOIU request) {
        String accessToken = request.getAccessToken();
        String refreshToken = request.getRefreshToken();
        userService.logout(accessToken, refreshToken);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, "/logout", null, "Logged out successfully"));
    }

    @PostMapping(path = "/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTOIU request, Principal principal) {
        userService.resetPassword(request, principal);

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/reset-password", null,
                "Password reset successfully"));
    }

    @PostMapping(path = "/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        if (token == null) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        userService.verifyEmail(token);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/verify-email", null,
                "Email verified successfully"));
    }

    @PostMapping(path = "/verify-email/send")
    public ResponseEntity<?> sendVerifyEmail(Principal principal) {
        userService.sendVerifyEmail(principal);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/verify-email/send", null,
                "Verify email sent successfully"));
    }
}
