package com.example.apps.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.dtos.RefreshAccessTokenDTO;
import com.example.apps.auth.dtos.RefreshAccessTokenDTOIU;
import com.example.apps.auth.services.ITokenService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/public/auth/token")
public class TokenController {

    @Autowired
    private ITokenService tokenService;

    @PostMapping(path = "/refresh")
    public ResponseEntity<ApiTemplate<Void, RefreshAccessTokenDTO>> refreshAccessToken(
            @RequestBody @Valid RefreshAccessTokenDTOIU request,
            HttpServletRequest servletRequest) {
        String accessToken = tokenService.refreshAccessToken(request.getRefreshToken());
        RefreshAccessTokenDTO dto = new RefreshAccessTokenDTO(accessToken);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, dto));
    }

}
