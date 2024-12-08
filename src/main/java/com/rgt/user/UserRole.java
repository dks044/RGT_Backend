package com.rgt.user;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN(0, "ROLE_ADMIN"),
    USER(1, "ROLE_USER");

    UserRole(int key, String value) {
        this.key = key;
        this.value = value;
    }

    private int key;
    private String value;
}
