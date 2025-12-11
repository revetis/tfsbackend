package com.example.apps.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.dto.ForgotPasswordDTOIU;
import com.example.apps.auth.dto.ForgotPasswordSetNewPasswordDTOIU;
import com.example.apps.auth.dto.UserLoginDTO;
import com.example.apps.auth.dto.UserLoginDTOIU;
import com.example.apps.auth.dto.UserRegisterDTO;
import com.example.apps.auth.dto.UserRegisterDTOIU;
import com.example.apps.auth.services.IUserService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/public/auth")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping(path = "/register")
    public UserRegisterDTO registerUser(@RequestBody @Valid UserRegisterDTOIU request) {
        return userService.registerUser(request);
    }

    @PostMapping(path = "/login")
    public UserLoginDTO login(@RequestBody @Valid UserLoginDTOIU request) {
        return userService.login(request);
    }

    @PostMapping(path = "/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordDTOIU request) {
        if (request.getEmail() == null) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        userService.forgotPassword(request, null);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/forgot-password", null,
                "Password reset link sent to your email"));
    }

    @PutMapping(path = "/forgot-password/reset")
    public ResponseEntity<?> forgotPasswordSetNewPassword(@RequestParam String token,
            @RequestBody @Valid ForgotPasswordSetNewPasswordDTOIU newPassword) {
        if (token == null) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        userService.forgotPassword(null, newPassword, token);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/forgot-password/reset", null,
                "Password reset successfully"));
    }

}
