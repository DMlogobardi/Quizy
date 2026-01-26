package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

public class rispostaDTO {
    @SerializedName("id")
    private int id;


    @SerializedName("affermazione")
    private String affermazione;

    @SerializedName("flagRispostaCorretta")
    private Boolean flagRispostaCorretta;

    public rispostaDTO(String affermazione, Boolean flagRispostaCorretta) {
        this.affermazione = affermazione;
        this.flagRispostaCorretta = flagRispostaCorretta;
    }

    public int getId() {
        return id;
    }

    public String getAffermazione() {
        return affermazione;
    }

    public void setAffermazione(String affermazione) {
        this.affermazione = affermazione;
    }

    public Boolean getFlagRispostaCorretta() {
        return flagRispostaCorretta;
    }

    public void setFlagRispostaCorretta(Boolean flagRispostaCorretta) {
        this.flagRispostaCorretta = flagRispostaCorretta;
    }
}
