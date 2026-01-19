package model.dto;

import model.entity.Quiz;

import java.io.Serializable;

public class QuizDTO implements Serializable {
    private int id;
    private String tempo;
    private String difficolta;
    private String descrizione;
    private int numero_domande;
    private Boolean passwordRichiesta;

    public QuizDTO() {
    }

    public QuizDTO(Quiz quiz) {
        this.id = quiz.getId();
        this.numero_domande = quiz.getNumeroDomande();
        this.descrizione = quiz.getDescrizione();
        this.difficolta = quiz.getDifficolta();
        this.tempo = quiz.getTempo();

        String pass = quiz.getPasswordQuiz();
        this.passwordRichiesta = (pass != null && !pass.trim().isEmpty());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero_domande() {
        return numero_domande;
    }

    public void setNumero_domande(int numero_domande) {
        this.numero_domande = numero_domande;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDifficolta() {
        return difficolta;
    }

    public void setDifficolta(String difficolta) {
        this.difficolta = difficolta;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public Boolean getPasswordRichiesta() {
        return passwordRichiesta;
    }

    public void setPasswordRichiesta(Boolean passwordRichiesta) {
        this.passwordRichiesta = passwordRichiesta;
    }
}
