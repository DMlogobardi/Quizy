package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class DeleteQuizRequest {

    @SerializedName("id")
    private int id;

    public DeleteQuizRequest(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}