package com.iems5718.shop.security;

public final class SecurityConstants {

    public static final String AUTH_COOKIE = "iems5718_auth";
    public static final String CSRF_COOKIE = "iems5718_csrf";
    public static final String CURRENT_USER_ATTR = "currentUser";
    public static final String CSP_POLICY = "default-src 'self'; img-src 'self' data: blob:; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; font-src 'self' data:; connect-src 'self'; media-src 'self' blob:; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'; upgrade-insecure-requests";

    private SecurityConstants() {
    }
}
