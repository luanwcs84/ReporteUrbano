package com.example.reporteurbano;

public class AuthUser {
    private final String id;
    private final String email;

    public AuthUser(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
