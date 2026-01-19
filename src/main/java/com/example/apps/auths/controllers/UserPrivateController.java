package com.example.apps.auths.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.apps.auths.dtos.ResetPasswordDTOIU;

import com.example.apps.auths.dtos.UserUpdateDTOIU;
import com.example.apps.auths.securities.CustomUserDetails;
import com.example.apps.auths.services.IUserService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletResponse;
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
                                        .body(ApiTemplate.apiTemplateGenerator(false, 400, "/profile/avatar",
                                                        "File is empty", null));
                }
                Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
                userService.avatar(file, userId);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, "/profile/avatar", null,
                                                "Avatar uploaded successfully"));
        }

        @PutMapping(path = "/profile")
        public ResponseEntity<?> updateProfile(@RequestBody @Valid UserUpdateDTOIU request, Principal principal) {
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, "/profile", null,
                                                userService.updateProfile(request, principal)));

        }

        @PostMapping(path = "/logout")
        public ResponseEntity<?> logout(
                        @CookieValue(name = "refreshToken", required = false) String refreshToken,
                        @CookieValue(name = "accessToken", required = false) String accessToken,
                        HttpServletResponse response) {

                // Blacklist tokens if they exist
                if (accessToken != null && refreshToken != null) {
                        userService.logout(accessToken, refreshToken);
                }

                // Clear cookies by setting maxAge to 0
                ResponseCookie clearAccessCookie = ResponseCookie.from("accessToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .secure(false)
                                .build();

                ResponseCookie clearRefreshCookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .secure(false)
                                .build();

                return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, clearAccessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                                .body(ApiTemplate.apiTemplateGenerator(true, 200, "/logout", null,
                                                "Logged out successfully"));
        }

        @PostMapping(path = "/reset-password")
        public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTOIU request, Principal principal) {
                userService.resetPassword(request, principal);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/reset-password", null,
                                "Password reset successfully"));
        }

        @PostMapping(path = "/verify-email/send")
        public ResponseEntity<?> sendVerifyEmail(Principal principal) {
                userService.sendVerifyEmail(principal);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/verify-email/send", null,
                                "Verify email sent successfully"));
        }

        @PostMapping(path = "/delete-account")
        public ResponseEntity<?> deleteMyAccount(
                        @RequestBody @Valid com.example.apps.auths.dtos.DeleteAccountRequest request,
                        Principal principal,
                        HttpServletResponse response) {

                // Delete account
                userService.deleteMyAccount(request, principal);

                // Clear cookies
                ResponseCookie clearAccessCookie = ResponseCookie.from("accessToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .secure(false)
                                .build();

                ResponseCookie clearRefreshCookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .secure(false)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, clearAccessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                                .body(ApiTemplate.apiTemplateGenerator(true, 200, "/delete-account", null,
                                                "Account deleted successfully"));
        }

        @PostMapping(path = "/2fa/init-enable")
        public ResponseEntity<?> initEnableTwoFactor(Principal principal) {
                Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
                String verificationId = userService.initiateTwoFactorToggle(userId);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/2fa/init-enable", null,
                                verificationId));
        }

        @PostMapping(path = "/2fa/complete-enable")
        public ResponseEntity<?> completeEnableTwoFactor(
                        @RequestBody @Valid com.example.apps.auths.dtos.CompleteTwoFactorToggleRequest request,
                        Principal principal) {
                Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
                userService.completeTwoFactorToggle(userId, request.getVerificationId(), request.getCode(),
                                request.getEnable());

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/2fa/complete-enable", null,
                                "Two Factor Authentication enabled successfully"));
        }

        @PostMapping(path = "/2fa/disable")
        public ResponseEntity<?> disableTwoFactor(Principal principal) {
                Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
                // Passing dummy verificationId/code for disable as implemented in service
                userService.completeTwoFactorToggle(userId, null, null, false);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/2fa/disable", null,
                                "Two Factor Authentication disabled successfully"));
        }
}
