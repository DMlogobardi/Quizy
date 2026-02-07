package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class StartQuizNoPassRequest {

    @SerializedName("id")
    private int id;

    @SerializedName("passwordQuiz")
    private String passwordQuiz;


    public StartQuizNoPassRequest(int id) {
        this.id = id;
    }

    public StartQuizNoPassRequest(int id, String passwordQuiz) {
        this.id = id;
        this.passwordQuiz = passwordQuiz;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPasswordQuiz() {
        return passwordQuiz;
    }

    public void setPasswordQuiz(String passwordQuiz) {
        this.passwordQuiz = passwordQuiz;
    }
}
