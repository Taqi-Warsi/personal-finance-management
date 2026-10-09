package com.pfm.service;

import com.pfm.exception.UnauthorizedAccessException;
import com.pfm.model.AppUser;

public final class AccessControl {

    private AccessControl() {}

    // Polymorphism in action: each role's getPermissions() decides the outcome
    public static void require(AppUser user, String permission) {
        if (!user.getPermissions().contains(permission)) {
            throw new UnauthorizedAccessException(user.getRole() + " '" + user.getName()
                    + "' lacks permission " + permission);
        }
    }
}