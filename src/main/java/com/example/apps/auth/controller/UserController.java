package com.example.apps.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.dto.UserLoginDTO;
import com.example.apps.auth.dto.UserLoginDTOIU;
import com.example.apps.auth.dto.UserRegisterDTO;
import com.example.apps.auth.dto.UserRegisterDTOIU;
import com.example.apps.auth.services.IUserService;

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
}
