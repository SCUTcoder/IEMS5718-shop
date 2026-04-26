package com.iems5718.shop.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RequestValidationTest {

    private final RequestValidation validation = new RequestValidation();

    @Test
    void adminProductAndCategoryWritesAreNotCsrfExempt() {
        assertFalse(validation.isCsrfExemptPath("/api/products"));
        assertFalse(validation.isCsrfExemptPath("/api/products/1/weight"));
        assertFalse(validation.isCsrfExemptPath("/api/categories"));
        assertFalse(validation.isCsrfExemptPath("/api/categories/1"));
    }

    @Test
    void authAndWebhookEndpointsAreCsrfExempt() {
        assertTrue(validation.isCsrfExemptPath("/api/auth/login"));
        assertTrue(validation.isCsrfExemptPath("/api/auth/register"));
        assertTrue(validation.isCsrfExemptPath("/api/auth/csrf"));
        assertTrue(validation.isCsrfExemptPath("/api/checkout/webhook"));
    }

    @Test
    void adminWritePathCoversProductAndCategoryMutations() {
        assertTrue(validation.isAdminWritePath("/api/products", "POST"));
        assertTrue(validation.isAdminWritePath("/api/products/1", "PUT"));
        assertTrue(validation.isAdminWritePath("/api/products/1/weight", "PATCH"));
        assertTrue(validation.isAdminWritePath("/api/categories/1", "DELETE"));
        assertFalse(validation.isAdminWritePath("/api/products", "GET"));
        assertFalse(validation.isAdminWritePath("/api/checkout/create", "POST"));
    }
}
