package com.example.apps.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.dtos.RefreshAccessTokenDTO;
import com.example.apps.auth.dtos.RefreshAccessTokenDTOIU;
import com.example.apps.auth.services.ITokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/public/auth/token")
public class TokenController {

    @Autowired
    private ITokenService tokenService;

    @PostMapping(path = "/refresh")
    public RefreshAccessTokenDTO refreshAccessToken(@RequestBody @Valid RefreshAccessTokenDTOIU request) {
        String accessToken = tokenService.refreshAccessToken(request.getRefreshToken());
        return new RefreshAccessTokenDTO(accessToken);
    }

}
