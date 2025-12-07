package com.example.apps.auth.controller;


import com.example.apps.auth.dto.LoginRequestDTO;
import com.example.apps.auth.dto.RegisterRequestDTO;
import com.example.apps.auth.dto.RegisterRequestDTOIU;
import com.example.apps.auth.security.CustomUserDetailsService;
import com.example.apps.auth.services.IRegisterService;
import com.example.configuration.GlobalApiResult;
import com.example.exception.ApiError;
import com.example.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
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

    @Autowired
    IRegisterService registerService;

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO request){
        if (!request.getUsername().isBlank()){

        } else if (!request.getEmail().isBlank()) {

        } else if (!request.getPhoneNumber().isBlank()) {

        }else {
            throw new NullPointerException();
        }


        return null;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTOIU request){
        RegisterRequestDTO returnUser = registerService.register(request);

        if (returnUser != null){
            return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(GlobalApiResult.generate(null,returnUser));
        }

        return ResponseEntity.internalServerError().body(GlobalApiResult.generate(GlobalExceptionHandler.ApiErrorGenerator("Unknown Error"), null));
    }

}
