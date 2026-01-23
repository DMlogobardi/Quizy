package model.dto;

import model.entity.Quiz;
import model.entity.Risposta;

import java.io.Serializable;
import java.util.List;

public class CompletaQuizDTO implements Serializable {

    private List<Risposta> risposteClient;
    private Quiz quiz;

    public CompletaQuizDTO() {
    }

    public CompletaQuizDTO(List<Risposta> risposteClient, Quiz quiz) {
        this.risposteClient = risposteClient;
        this.quiz = quiz;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public List<Risposta> getRisposteClient() {
        return risposteClient;
    }

    public void setRisposteClient(List<Risposta> risposteClient) {
        this.risposteClient = risposteClient;
    }
}
