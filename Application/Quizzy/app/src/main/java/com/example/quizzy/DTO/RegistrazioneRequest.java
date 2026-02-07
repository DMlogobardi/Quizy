package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class RegistrazioneRequest {
    @SerializedName("nome")
    private String nome;

    @SerializedName("cognome")
    private String cognome;

    @SerializedName("username")
    private String username;

    @SerializedName("passwordHash")
    private String passwordHash;

    public RegistrazioneRequest(String nome, String username, String passwordHash, String cognome) {
        this.nome = nome;
        this.username = username;
        this.passwordHash = passwordHash;
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
}
