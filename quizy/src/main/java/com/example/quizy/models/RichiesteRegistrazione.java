package com.example.quizy.models;

public class RichiesteRegistrazione {
    private String nome;
    private String cognome;
    private String dataNascita;
    private String username;
    private String password;

    public RichiesteRegistrazione(String nome, String cognome, String dataNascita, String username, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(String dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}