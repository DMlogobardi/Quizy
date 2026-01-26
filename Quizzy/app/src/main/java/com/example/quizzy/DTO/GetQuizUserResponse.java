package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetQuizUserResponse {
    ArrayList<ListQuizDTO> quiz;

    public ArrayList<ListQuizDTO> getQuiz() {
        return quiz;
    }
}
