package com.example.apps.auth.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.dtos.RefreshAccessTokenDTO;
import com.example.apps.auth.dtos.RefreshAccessTokenDTOIU;
import com.example.apps.auth.services.ITokenService;
import com.example.tfs.maindto.ApiTemplate;

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
        String ipAddress = servletRequest.getHeader("X-Forwarded-For");
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0];
        }
        if (ipAddress == null) {
            ipAddress = servletRequest.getRemoteAddr();
        }

        Map<String, String> tokens = tokenService.refreshAccessToken(request.getRefreshToken(), ipAddress);

        RefreshAccessTokenDTO dto = new RefreshAccessTokenDTO(tokens.get("refreshToken"), tokens.get("accessToken"));

        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, dto));
    }

}
