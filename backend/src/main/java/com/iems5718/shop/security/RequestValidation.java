package com.iems5718.shop.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RequestValidation {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    public boolean requiresCsrf(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod());
    }

    public boolean isPublicPath(String path) {
        return isCsrfExemptPath(path)
                || path.startsWith("/images/")
                || path.startsWith("/videos/")
                || path.startsWith("/api/products")
                || path.startsWith("/api/categories");
    }

    public boolean isCsrfExemptPath(String path) {
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/csrf")
                || path.equals("/api/checkout/webhook");
    }

    public boolean isAdminWritePath(String path, String method) {
        if (Set.of("POST", "PUT", "PATCH", "DELETE").contains(method)) {
            return path.startsWith("/api/products") || path.startsWith("/api/categories");
        }
        return false;
    }

    public boolean isAuthProtectedPath(String path) {
        return path.equals("/api/auth/logout")
                || path.equals("/api/auth/change-password");
    }
}
