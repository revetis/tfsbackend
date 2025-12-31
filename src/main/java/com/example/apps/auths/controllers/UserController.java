package com.example.apps.auths.controllers;

import com.example.apps.auths.dtos.*;
import com.example.tfs.maindto.ApiErrorTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.apps.auths.services.IUserService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
@RestController
@RequestMapping("/rest/api/public/auth")
public class UserController {

        @Autowired
        private IUserService userService;

        @PostMapping(path = "/register")
        public ResponseEntity<ApiTemplate<Void, UserRegisterDTO>> registerUser(
                        @RequestBody @Valid UserRegisterDTOIU request,
                        HttpServletRequest servletRequest) {
                UserRegisterDTO registeredUser = userService.registerUser(request);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                registeredUser));
        }

        @PostMapping(path = "/login")
        public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTOIU request,
                        HttpServletRequest servletRequest) {
                String ipAddress = servletRequest.getHeader("X-Forwarded-For");
                if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0];
                }
                if (ipAddress == null) {
                        ipAddress = servletRequest.getRemoteAddr();
                }

                UserLoginDTO loggedInUser = userService.login(request, ipAddress);

                ResponseCookie accessCookie = ResponseCookie.from("accessToken", loggedInUser.getAccessToken())
                                .httpOnly(true)
                                .secure(false).path("/").sameSite("Lax").maxAge(60 * 60).build();
                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loggedInUser.getRefreshToken())
                                .httpOnly(true).secure(false).path("/").sameSite("Lax").maxAge(7 * 24 * 60 * 60)
                                .build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                .body(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                userService.getUserByUsernameOrEmail(request.getUsernameOrEmail())));
        }

        @PostMapping(path = "/forgot-password")
        public ResponseEntity<ApiTemplate<Void, String>> forgotPassword(@RequestBody @Valid ForgotPasswordDTOIU request,
                        HttpServletRequest servletRequest) {
                if (request.getEmail() == null) {
                        throw new IllegalArgumentException("Email is required");
                }
                userService.forgotPassword(request, null);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "Password reset link sent to your email"));
        }

        @PutMapping(path = "/forgot-password/reset")
        public ResponseEntity<ApiTemplate<Void, String>> forgotPasswordSetNewPassword(@RequestParam String token,
                        @RequestBody @Valid ForgotPasswordSetNewPasswordDTOIU newPassword,
                        HttpServletRequest servletRequest) {
                if (token == null) {
                        throw new IllegalArgumentException("Token is required");
                }
                userService.forgotPassword(null, newPassword, token);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "Password reset successfully"));
        }

        @GetMapping("/access-check")
        public ResponseEntity<?> accessCheck(
                        @CookieValue(name = "refreshToken") String refreshToken,
                        @CookieValue(name = "accessToken", required = false) String accessToken,
                        HttpServletRequest servletRequest) {

                String ipAddress = servletRequest.getHeader("X-Forwarded-For");
                if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0];
                }
                if (ipAddress == null) {
                        ipAddress = servletRequest.getRemoteAddr();
                }

                AccessCheckDTO access = userService.accessCheck(refreshToken, accessToken, ipAddress);

                if (!access.getIsPermitted()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.FORBIDDEN.value(),
                                                        "/access-check",
                                                        "Not authorized"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                servletRequest.getRequestURI(),
                                                null,
                                                "Authorized"));
        }

}
