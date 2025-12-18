package com.example.apps.auths.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auths.dtos.ForgotPasswordDTOIU;
import com.example.apps.auths.dtos.ForgotPasswordSetNewPasswordDTOIU;
import com.example.apps.auths.dtos.UserLoginDTO;
import com.example.apps.auths.dtos.UserLoginDTOIU;
import com.example.apps.auths.dtos.UserRegisterDTO;
import com.example.apps.auths.dtos.UserRegisterDTOIU;
import com.example.apps.auths.services.IUserService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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
        public ResponseEntity<ApiTemplate<Void, UserLoginDTO>> login(@RequestBody @Valid UserLoginDTOIU request,
                        HttpServletRequest servletRequest) {
                String ipAddress = servletRequest.getHeader("X-Forwarded-For");
                if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0];
                }
                if (ipAddress == null) {
                        ipAddress = servletRequest.getRemoteAddr();
                }

                UserLoginDTO loggedInUser = userService.login(request, ipAddress);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                loggedInUser));
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

}
