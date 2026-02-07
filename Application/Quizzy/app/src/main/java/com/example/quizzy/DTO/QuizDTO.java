package com.example.quizzy.DTO;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class QuizDTO {

    @SerializedName("id")
    @Nullable
    private Integer id;
    @SerializedName("titolo")
    private String titolo;

    @SerializedName("descrizione")
    private String descrizione;

    @SerializedName("difficolta")
    private String difficolta;

    @SerializedName("tempo")
    private String tempo;

    @SerializedName("numeroDomande")
    private int numeroDomande;

    @SerializedName("passwordQuiz")
    private String passwordQuiz;

    @SerializedName("domande")
    private ArrayList<domandaDTO> domande = new ArrayList<>();

    public QuizDTO(String titolo, String descrizione, String difficolta, String tempo, int numeroDomande, String passwordQuiz) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.difficolta = difficolta;
        this.tempo = tempo;
        this.numeroDomande = numeroDomande;
        this.passwordQuiz = passwordQuiz;
    }

    public QuizDTO(String titolo, String descrizione, String difficolta, String tempo, int numeroDomande, String passwordQuiz, ArrayList<domandaDTO> domande) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.difficolta = difficolta;
        this.tempo = tempo;
        this.numeroDomande = numeroDomande;
        this.passwordQuiz = passwordQuiz;
        this.domande = domande;
    }

    public domandaDTO addDomanda(domandaDTO domanda){
        this.domande.add(domanda);
        return domanda;
    }

    public domandaDTO removeDomanda(domandaDTO domanda){
        this.domande.remove(domanda);
        return domanda;
    }

    public ArrayList<domandaDTO> getDomande() {
        return domande;
    }

    public void setDomande(ArrayList<domandaDTO> domande) {
        this.domande = domande;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
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

    public int getNumeroDomande() {
        return numeroDomande;
    }

    public void setNumeroDomande(int numeroDomande) {
        this.numeroDomande = numeroDomande;
    }

    public String getPasswordQuiz() {
        return passwordQuiz;
    }

    public void setPasswordQuiz(String passwordQuiz) {
        this.passwordQuiz = passwordQuiz;
    }
}
