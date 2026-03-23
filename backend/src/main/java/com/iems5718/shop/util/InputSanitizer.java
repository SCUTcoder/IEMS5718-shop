package com.iems5718.shop.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class InputSanitizer {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N} .,'-]{2,80}$");

    private InputSanitizer() {
    }

    public static String normalizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        normalized = CONTROL_CHARS.matcher(normalized).replaceAll("");
        normalized = normalized.trim().replaceAll("\\s{2,}", " ");
        if (normalized.length() > maxLength) {
            normalized = normalized.substring(0, maxLength).trim();
        }
        return normalized;
    }

    public static String requireText(String value, int maxLength, String fieldName) {
        String normalized = normalizeText(value, maxLength);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    public static String requireEmail(String value) {
        String email = requireText(value, 255, "Email").toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        return email;
    }

    public static String requireDisplayName(String value) {
        String name = requireText(value, 80, "Display name");
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Display name contains unsupported characters");
        }
        return name;
    }

    public static String requirePassword(String value) {
        if (value == null || value.length() < 8 || value.length() > 72) {
            throw new IllegalArgumentException("Password must be 8-72 characters");
        }
        return value;
    }
}
