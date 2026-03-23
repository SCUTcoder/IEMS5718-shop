package com.iems5718.shop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SecurityCookieService {

    private static final int COOKIE_MAX_AGE = (int) Duration.ofHours(48).getSeconds();

    public void setAuthCookie(HttpServletRequest request, HttpServletResponse response, String value) {
        addCookie(request, response, SecurityConstants.AUTH_COOKIE, value, true, COOKIE_MAX_AGE);
    }

    public void setCsrfCookie(HttpServletRequest request, HttpServletResponse response, String value) {
        addCookie(request, response, SecurityConstants.CSRF_COOKIE, value, false, COOKIE_MAX_AGE);
    }

    public void clearAuthCookies(HttpServletRequest request, HttpServletResponse response) {
        addCookie(request, response, SecurityConstants.AUTH_COOKIE, "", true, 0);
        addCookie(request, response, SecurityConstants.CSRF_COOKIE, "", false, 0);
    }

    private void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
                           boolean httpOnly, int maxAge) {
        boolean secure = SecurityUtils.isSecureRequest(request);
        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append("=").append(value == null ? "" : value)
                .append("; Path=/")
                .append("; Max-Age=").append(maxAge)
                .append("; SameSite=Lax");

        if (httpOnly) {
            cookie.append("; HttpOnly");
        }
        if (secure) {
            cookie.append("; Secure");
        }

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
