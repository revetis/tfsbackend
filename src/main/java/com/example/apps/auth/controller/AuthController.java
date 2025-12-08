package com.example.apps.auth.controller;


import com.example.apps.auth.dto.LoginRequestDTO;
import com.example.apps.auth.dto.LoginRequestDTOIU;
import com.example.apps.auth.dto.RegisterRequestDTO;
import com.example.apps.auth.dto.RegisterRequestDTOIU;
import com.example.apps.auth.security.CustomUserDetailsService;
import com.example.apps.auth.security.PasswordEncoderConfigurations;
import com.example.apps.auth.security.SecurityConfiguration;
import com.example.apps.auth.services.ILoginService;
import com.example.apps.auth.services.IRegisterService;
import com.example.configuration.GlobalApiResult;
import com.example.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/rest/api/public/auth")
public class AuthController {

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    IRegisterService registerService;

    @Autowired
    ILoginService loginService;

    @Autowired
    SecurityConfiguration securityConfiguration;

    @Autowired
    PasswordEncoderConfigurations passwordEncoder;

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTOIU request) throws Exception {

        LoginRequestDTO result =  loginService.loginService(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(GlobalApiResult.generate(null,result));
    }

    @PostMapping(path = "/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTOIU request){
        RegisterRequestDTO returnUser = registerService.register(request);

        if (returnUser != null){
            return ResponseEntity.status(HttpStatus.CREATED).body(GlobalApiResult.generate(null,returnUser));
        }

        return ResponseEntity.internalServerError().body(GlobalApiResult.generate(GlobalExceptionHandler.ApiErrorGenerator("Unknown Error"), null));
    }

}
