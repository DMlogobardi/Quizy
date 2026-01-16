package com.example.quizy.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    // Il backend invia una mappa: response.put("token", token);
    // Quindi il campo JSON si chiama "token"
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }
}