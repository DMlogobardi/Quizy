package com.example.quizzy.DTO;


import com.google.gson.annotations.SerializedName;

public class CompletaQuizResponse {

    @SerializedName("punteggio")
    private int punteggio;

    public int getPunteggio() {
        return punteggio;
    }
}
