package com.example.reporteurbano;

public class AuthUser {
    private final String id;
    private final String email;
    private final String nome;

    public AuthUser(String id, String email, String nome) {
        this.id = id;
        this.email = email;
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }
}
