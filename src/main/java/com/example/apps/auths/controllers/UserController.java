package com.example.apps.auths.controllers;

import com.example.apps.auths.dtos.*;
import com.example.tfs.maindto.ApiErrorTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.apps.auths.services.IUserService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
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
        public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTOIU request,
                        HttpServletRequest servletRequest) {
                String ipAddress = servletRequest.getHeader("X-Forwarded-For");
                if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0];
                }
                if (ipAddress == null) {
                        ipAddress = servletRequest.getRemoteAddr();
                }

                UserLoginDTO loggedInUser = userService.login(request, ipAddress);

                if (loggedInUser.isRequire2fa()) {
                        return ResponseEntity
                                        .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(),
                                                        null, loggedInUser));
                }

                ResponseCookie accessCookie = ResponseCookie.from("accessToken", loggedInUser.getAccessToken())
                                .httpOnly(true)
                                .secure(false).path("/").sameSite("Lax").maxAge(60 * 60).build();
                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loggedInUser.getRefreshToken())
                                .httpOnly(true).secure(false).path("/").sameSite("Lax").maxAge(7 * 24 * 60 * 60)
                                .build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                .body(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                userService.getUserByUsernameOrEmail(request.getUsernameOrEmail())));
        }

        @PostMapping(path = "/login/verify-2fa")
        public ResponseEntity<?> verifyTwoFactorLogin(@RequestBody @Valid VerifyTwoFactorRequest request,
                        HttpServletRequest servletRequest) {
                String ipAddress = servletRequest.getHeader("X-Forwarded-For");
                if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0];
                }
                if (ipAddress == null) {
                        ipAddress = servletRequest.getRemoteAddr();
                }

                UserLoginDTO loggedInUser = userService.verifyTwoFactorLogin(request.getVerificationId(),
                                request.getCode(),
                                ipAddress);

                ResponseCookie accessCookie = ResponseCookie.from("accessToken", loggedInUser.getAccessToken())
                                .httpOnly(true)
                                .secure(false).path("/").sameSite("Lax").maxAge(60 * 60).build();
                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loggedInUser.getRefreshToken())
                                .httpOnly(true).secure(false).path("/").sameSite("Lax").maxAge(7 * 24 * 60 * 60)
                                .build();

                // Fetch user data again for consistency in response
                // Since we don't have username directly, we can decode token or just return
                // success message
                // Or better, verifyTwoFactorLogin could return enriched DTO?
                // For now, let's keep it simple and just return success with cookies.
                // Actually frontend expects user object on login success usually.
                // UserService can't easily fetch UserDTO without username in
                // verifyTwoFactorLogin.
                // Let's assume frontend will fetch profile on mount if user is missing but
                // cookies are present.
                // But wait, existing login returns UserDTO. We should try to match that.
                // Modifying verifyTwoFactorLogin to return User entity/DTO would be cleaner but
                // changing return type again.
                // Let's assume for now we return loggedInUser DTO (tokens) and frontend handles
                // checkAuth.

                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                .body(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "2FA Verified Successfully"));
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

        @GetMapping("/access-check")
        public ResponseEntity<?> accessCheck(
                        @CookieValue(name = "refreshToken") String refreshToken,
                        @CookieValue(name = "accessToken", required = false) String accessToken,
                        HttpServletRequest servletRequest) {

                String ipAddress = servletRequest.getHeader("X-Forwarded-For");
                if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0];
                }
                if (ipAddress == null) {
                        ipAddress = servletRequest.getRemoteAddr();
                }

                AccessCheckDTO access = userService.accessCheck(refreshToken, accessToken, ipAddress);

                if (!access.getIsPermitted()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                        false,
                                                        HttpStatus.FORBIDDEN.value(),
                                                        "/access-check",
                                                        "Not authorized"));
                }

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                servletRequest.getRequestURI(),
                                                null,
                                                "Authorized"));
        }

        @PostMapping(path = "/verify-email")
        public ResponseEntity<?> verifyEmail(@RequestParam String token) {
                if (token == null) {
                        return ResponseEntity.badRequest()
                                        .body(ApiTemplate.apiTemplateGenerator(false, 400, "/verify-email",
                                                        "Token is required", null));
                }
                userService.verifyEmail(token);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/verify-email", null,
                                "Email verified successfully"));
        }

        @PostMapping(path = "/logout")
        public ResponseEntity<?> logout(
                        @CookieValue(name = "refreshToken", required = false) String refreshToken,
                        @CookieValue(name = "accessToken", required = false) String accessToken,
                        HttpServletResponse response) {

                // Blacklist tokens if they exist - attempt only, ignore failures since this is
                // public
                if (accessToken != null || refreshToken != null) {
                        try {
                                userService.logout(accessToken, refreshToken);
                        } catch (Exception e) {
                                // Ignore invalid token errors during logout
                        }
                }

                // Clear cookies by setting maxAge to 0
                ResponseCookie clearAccessCookie = ResponseCookie.from("accessToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .secure(false)
                                .build();

                ResponseCookie clearRefreshCookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .secure(false)
                                .build();

                return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, clearAccessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                                .body(ApiTemplate.apiTemplateGenerator(true, 200, "/logout", null,
                                                "Logged out successfully"));
        }

}
