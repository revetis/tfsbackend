package com.example.apps.auths.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.apps.auths.dtos.RefreshAccessTokenDTOIU;
import com.example.apps.auths.services.ITokenService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/rest/api/public/auth/token")
public class TokenController {

        @Autowired
        private ITokenService tokenService;

        @PostMapping("/refresh")
        public ResponseEntity<?> refreshAccessToken(
                        @RequestBody(required = false) RefreshAccessTokenDTOIU request,
                        @CookieValue(name = "accessToken", required = false) String accessToken,
                        HttpServletRequest servletRequest) {

                String refreshToken = null;
                if (request != null && request.getRefreshToken() != null) {
                        refreshToken = request.getRefreshToken();
                } else {
                        // Check cookies
                        if (servletRequest.getCookies() != null) {
                                for (var cookie : servletRequest.getCookies()) {
                                        if ("refreshToken".equals(cookie.getName())) {
                                                refreshToken = cookie.getValue();
                                                break;
                                        }
                                }
                        }
                }

                if (refreshToken == null) {
                        return ResponseEntity.badRequest().body(ApiTemplate.apiTemplateGenerator(
                                        false, 400, servletRequest.getRequestURI(), "refreshToken is missing", null));
                }

                String ipAddress = servletRequest.getHeader("X-Forwarded-For");
                if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0];
                }
                if (ipAddress == null) {
                        ipAddress = servletRequest.getRemoteAddr();
                }

                Map<String, String> tokens = tokenService.refreshAccessToken(refreshToken, accessToken, ipAddress);

                ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokens.get("accessToken"))
                                .httpOnly(true)
                                .path("/")
                                .maxAge(60 * 60)
                                .sameSite("Lax")
                                .secure(false) // prod’da true
                                .build();

                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                                .httpOnly(true)
                                .path("/")
                                .maxAge(7 * 24 * 60 * 60)
                                .sameSite("Lax")
                                .secure(false)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                .body(ApiTemplate.apiTemplateGenerator(
                                                true,
                                                200,
                                                servletRequest.getRequestURI(),
                                                null,
                                                "refresh success"));
        }

}
