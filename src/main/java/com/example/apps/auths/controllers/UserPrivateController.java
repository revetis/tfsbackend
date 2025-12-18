package com.example.apps.auths.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.apps.auths.dtos.LogoutRequestDTOIU;
import com.example.apps.auths.dtos.ResetPasswordDTOIU;
import com.example.apps.auths.dtos.UserUpdateDTOIU;
import com.example.apps.auths.securities.CustomUserDetails;
import com.example.apps.auths.services.IUserService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/private/auth")
public class UserPrivateController {

    @Autowired
    private IUserService userService;

    @GetMapping(path = "/profile")
    public ResponseEntity<?> profile(Principal principal) {
        Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();

        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, 200, "/profile/" + principal.getName(), null,
                        userService.profile(principal.getName(), userId)));
    }

    @PostMapping(path = "/profile/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiTemplate.apiTemplateGenerator(false, 400, "/profile/avatar", "File is empty", null));
        }
        Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
        userService.avatar(file, userId);
        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, 200, "/profile/avatar", null, "Avatar uploaded successfully"));
    }

    @PutMapping(path = "/profile")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid UserUpdateDTOIU request, Principal principal) {
        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, 200, "/profile", null,
                        userService.updateProfile(request, principal)));

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
            return ResponseEntity.badRequest()
                    .body(ApiTemplate.apiTemplateGenerator(false, 400, "/verify-email", "Token is required", null));
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
