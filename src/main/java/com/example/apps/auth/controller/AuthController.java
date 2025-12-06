package com.example.apps.auth.controller;


import com.example.apps.auth.dto.LoginRequestDTO;
import com.example.apps.auth.dto.RegisterRequestDTO;
import com.example.apps.auth.repository.IUserRepository;
import com.example.apps.auth.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/rest/public/auth")
public class AuthController {

    @Autowired
    CustomUserDetailsService userDetailsService;

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request){
        if (!request.getUsername().isBlank()){

        } else if (!request.getEmail().isBlank()) {

        } else if (!request.getPhoneNumber().isBlank()) {

        }else {
            throw new NullPointerException();
        }


        return null;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request){
        userDetailsService.
    }

}
