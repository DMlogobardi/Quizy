package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class ListQuizDTO {
    @SerializedName("id")
    private int id;

    @SerializedName("titolo")
    private String titolo;

    @SerializedName("tempo")
    private String tempo;
    @SerializedName("difficolta")
    private String difficolta;

    @SerializedName("descrizione")
    private String descrizione;
    @SerializedName("numero_domande")
    private int numero_domande;

    @SerializedName("passwordRichiesta")
    private boolean passwordRichiesta;

    public ListQuizDTO(int id, String titolo, String tempo, String difficolta, String descrizione, int numero_domande, boolean passwordRichiesta) {
        this.id = id;
        this.titolo = titolo;
        this.tempo = tempo;
        this.difficolta = difficolta;
        this.descrizione = descrizione;
        this.numero_domande = numero_domande;
        this.passwordRichiesta = passwordRichiesta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public String getDifficolta() {
        return difficolta;
    }

    public void setDifficolta(String difficolta) {
        this.difficolta = difficolta;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public int getNumero_domande() {
        return numero_domande;
    }

    public void setNumero_domande(int numero_domande) {
        this.numero_domande = numero_domande;
    }

    public boolean isPasswordRichiesta() {
        return passwordRichiesta;
    }

    public void setPasswordRichiesta(boolean passwordRichiesta) {
        this.passwordRichiesta = passwordRichiesta;
    }
}
