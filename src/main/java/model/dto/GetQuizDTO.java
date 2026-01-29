package model.dto;

import java.io.Serializable;

public class GetQuizDTO implements Serializable {
    private int page;

    public GetQuizDTO() {
    }

    public GetQuizDTO(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
