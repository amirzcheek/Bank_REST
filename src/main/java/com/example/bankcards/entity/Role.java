package com.example.bankcards.entity;

public enum Role {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");
    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return name();
    }
}