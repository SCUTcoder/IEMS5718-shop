package com.iems5718.shop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iems5718.shop.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final RequestValidation requestValidation;
    private final ObjectMapper objectMapper;

    public SecurityFilter(AuthService authService, RequestValidation requestValidation, ObjectMapper objectMapper) {
        this.authService = authService;
        this.requestValidation = requestValidation;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        applySecurityHeaders(response);

        Optional<CurrentUser> currentUser = authService.getCurrentUser(request);

        if (requestValidation.isAuthProtectedPath(path) && currentUser.isEmpty()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        if (requestValidation.isAdminWritePath(path, method)) {
            if (currentUser.isEmpty() || !currentUser.get().isAdmin()) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                return;
            }
        }

        if (requestValidation.isCsrfExemptPath(path) && requestValidation.requiresCsrf(request)) {
            // Public endpoints (e.g., payment webhooks) skip CSRF validation
            filterChain.doFilter(request, response);
            return;
        }

        if (requestValidation.requiresCsrf(request)) {
            String cookieToken = SecurityUtils.readCookie(request, SecurityConstants.CSRF_COOKIE);
            String requestToken = request.getHeader("X-CSRF-Token");
            if (requestToken == null || requestToken.isBlank()) {
                requestToken = request.getParameter("_csrf");
            }

            boolean tokenMatches = cookieToken != null && cookieToken.equals(requestToken);
            if (currentUser.isPresent()) {
                tokenMatches = tokenMatches && currentUser.get().session().getCsrfToken().equals(requestToken);
            }

            if (!tokenMatches) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void applySecurityHeaders(HttpServletResponse response) {
        response.setHeader("Content-Security-Policy", SecurityConstants.CSP_POLICY);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "status", status,
                "message", message
        ));
    }
}
