package com.iems5718.shop.security;

import com.iems5718.shop.model.AuthSession;
import com.iems5718.shop.model.User;

public record CurrentUser(User user, AuthSession session) {
    public boolean isAdmin() {
        return Boolean.TRUE.equals(user.getAdmin());
    }
}
