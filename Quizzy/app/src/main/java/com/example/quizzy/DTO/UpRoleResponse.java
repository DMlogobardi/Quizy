package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class UpRoleResponse {
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

}
