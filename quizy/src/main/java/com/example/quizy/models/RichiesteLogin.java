package com.example.quizy.models;

public class RichiesteLogin {
    private String username;
    private String passwordHash;

    public RichiesteLogin(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}