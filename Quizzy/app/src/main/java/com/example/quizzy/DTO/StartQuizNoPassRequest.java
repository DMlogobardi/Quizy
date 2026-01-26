package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class StartQuizNoPassRequest {

    @SerializedName("id")
    private int id;

    public StartQuizNoPassRequest(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
