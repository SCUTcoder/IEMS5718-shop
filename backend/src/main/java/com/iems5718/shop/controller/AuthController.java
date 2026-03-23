package com.iems5718.shop.controller;

import com.iems5718.shop.security.CurrentUser;
import com.iems5718.shop.security.SecurityConstants;
import com.iems5718.shop.security.SecurityCookieService;
import com.iems5718.shop.security.SecurityUtils;
import com.iems5718.shop.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final SecurityCookieService securityCookieService;

    public AuthController(AuthService authService, SecurityCookieService securityCookieService) {
        this.authService = authService;
        this.securityCookieService = securityCookieService;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> ensureCsrf(HttpServletRequest request, HttpServletResponse response) {
        String token = authService.getCurrentUser(request)
                .map(currentUser -> currentUser.session().getCsrfToken())
                .orElseGet(() -> authService.ensureGuestCsrfToken(SecurityUtils.readCookie(request, SecurityConstants.CSRF_COOKIE)));
        securityCookieService.setCsrfCookie(request, response, token);
        return ResponseEntity.ok(Map.of("csrfToken", token));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request, HttpServletResponse response) {
        Optional<CurrentUser> currentUser = authService.getCurrentUser(request);
        currentUser.ifPresent(user -> securityCookieService.setCsrfCookie(request, response, user.session().getCsrfToken()));
        return ResponseEntity.ok(authService.buildProfileResponse(currentUser));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest body) {
        authService.register(body.getDisplayName(), body.getEmail(), body.getPassword(), body.getConfirmPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest body,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        AuthService.AuthTokens tokens = authService.login(body.getEmail(), body.getPassword());
        securityCookieService.setAuthCookie(request, response, tokens.authToken());
        securityCookieService.setCsrfCookie(request, response, tokens.csrfToken());
        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "displayName", tokens.user().getDisplayName(),
                "admin", Boolean.TRUE.equals(tokens.user().getAdmin())
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        CurrentUser currentUser = authService.getCurrentUser(request)
                .orElseThrow(() -> new IllegalArgumentException("Authentication required"));
        authService.logout(currentUser);
        securityCookieService.clearAuthCookies(request, response);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest body,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {
        CurrentUser currentUser = authService.getCurrentUser(request)
                .orElseThrow(() -> new IllegalArgumentException("Authentication required"));
        authService.changePassword(currentUser, body.getCurrentPassword(), body.getNewPassword(), body.getConfirmPassword());
        securityCookieService.clearAuthCookies(request, response);
        return ResponseEntity.ok(Map.of("message", "Password updated. Please sign in again."));
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String displayName;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String confirmPassword;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;
        @NotBlank
        private String confirmPassword;
    }
}
