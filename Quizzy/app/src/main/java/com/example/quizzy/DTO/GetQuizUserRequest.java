package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

// GetQuizUserRequest.java
public class GetQuizUserRequest {
    @SerializedName("page")
    private int page;

    public GetQuizUserRequest(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
