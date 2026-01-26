package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class GetQuizRequest {
    @SerializedName("page")
    private int page;

    public GetQuizRequest(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
