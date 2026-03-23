package com.iems5718.shop.service;

import com.iems5718.shop.model.AuthSession;
import com.iems5718.shop.model.User;
import com.iems5718.shop.repository.AuthSessionRepository;
import com.iems5718.shop.repository.UserRepository;
import com.iems5718.shop.security.CurrentUser;
import com.iems5718.shop.security.SecurityConstants;
import com.iems5718.shop.security.SecurityUtils;
import com.iems5718.shop.util.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);
    private static final int SESSION_HOURS = 48;

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;

    public AuthService(UserRepository userRepository, AuthSessionRepository authSessionRepository) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
    }

    @Transactional
    public User register(String displayName, String email, String password, String confirmPassword) {
        String safeName = InputSanitizer.requireDisplayName(displayName);
        String safeEmail = InputSanitizer.requireEmail(email);
        String safePassword = InputSanitizer.requirePassword(password);
        InputSanitizer.requirePassword(confirmPassword);

        if (!safePassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByEmail(safeEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setDisplayName(safeName);
        user.setEmail(safeEmail);
        user.setPasswordHash(PASSWORD_ENCODER.encode(safePassword));
        user.setAdmin(false);
        return userRepository.save(user);
    }

    @Transactional
    public AuthTokens login(String email, String password) {
        String safeEmail = InputSanitizer.requireEmail(email);
        String safePassword = InputSanitizer.requirePassword(password);

        User user = userRepository.findByEmail(safeEmail)
                .orElseThrow(() -> new IllegalArgumentException("Email or password is incorrect"));

        if (!PASSWORD_ENCODER.matches(safePassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Email or password is incorrect");
        }

        authSessionRepository.deleteByUser(user);
        authSessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        String rawToken = SecurityUtils.randomToken();
        String csrfToken = SecurityUtils.randomToken();

        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenHash(SecurityUtils.sha256(rawToken));
        session.setCsrfToken(csrfToken);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(SESSION_HOURS));
        authSessionRepository.save(session);

        return new AuthTokens(rawToken, csrfToken, user);
    }

    @Transactional
    public void logout(CurrentUser currentUser) {
        authSessionRepository.delete(currentUser.session());
    }

    @Transactional
    public void changePassword(CurrentUser currentUser, String currentPassword, String newPassword, String confirmPassword) {
        InputSanitizer.requirePassword(currentPassword);
        String safeNewPassword = InputSanitizer.requirePassword(newPassword);
        InputSanitizer.requirePassword(confirmPassword);

        User user = currentUser.user();
        if (!PASSWORD_ENCODER.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!safeNewPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPasswordHash(PASSWORD_ENCODER.encode(safeNewPassword));
        userRepository.save(user);
        authSessionRepository.deleteByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<CurrentUser> getCurrentUser(HttpServletRequest request) {
        Object existing = request.getAttribute(SecurityConstants.CURRENT_USER_ATTR);
        if (existing instanceof CurrentUser currentUser) {
            return Optional.of(currentUser);
        }

        String rawToken = SecurityUtils.readCookie(request, SecurityConstants.AUTH_COOKIE);
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        Optional<AuthSession> sessionOpt = authSessionRepository.findByTokenHash(SecurityUtils.sha256(rawToken));
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        AuthSession session = sessionOpt.get();
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        CurrentUser currentUser = new CurrentUser(session.getUser(), session);
        request.setAttribute(SecurityConstants.CURRENT_USER_ATTR, currentUser);
        return Optional.of(currentUser);
    }

    public String ensureGuestCsrfToken(String currentCookieValue) {
        if (currentCookieValue != null && !currentCookieValue.isBlank()) {
            return currentCookieValue;
        }
        return SecurityUtils.randomToken();
    }

    public Map<String, Object> buildProfileResponse(Optional<CurrentUser> currentUser) {
        if (currentUser.isEmpty()) {
            return Map.of(
                    "authenticated", false,
                    "displayName", "Guest",
                    "email", "",
                    "admin", false
            );
        }

        User user = currentUser.get().user();
        return Map.of(
                "authenticated", true,
                "displayName", user.getDisplayName(),
                "email", user.getEmail(),
                "admin", Boolean.TRUE.equals(user.getAdmin())
        );
    }

    public record AuthTokens(String authToken, String csrfToken, User user) {
    }
}
