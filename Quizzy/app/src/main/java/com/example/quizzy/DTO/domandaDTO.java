package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class domandaDTO {

    @SerializedName("id")
    private int id;

    @SerializedName("quesito")
    private String quesito;

    @SerializedName("puntiRispostaCorretta")
    private int puntiRispostaCorretta;

    @SerializedName("puntiRispostaSbagliata")
    private int puntiRispostaSbagliata;

    @SerializedName("risposte")
    private ArrayList<rispostaDTO> risposte = new ArrayList<rispostaDTO>();

    public domandaDTO(String quesito, int puntiRispostaCorretta, int puntiRispostaSbagliata) {
        this.quesito = quesito;
        this.puntiRispostaCorretta = puntiRispostaCorretta;
        this.puntiRispostaSbagliata = puntiRispostaSbagliata;
    }

    public domandaDTO(String quesito, int puntiRispostaCorretta, int puntiRispostaSbagliata, ArrayList<rispostaDTO> risposte) {
        this.quesito = quesito;
        this.puntiRispostaCorretta = puntiRispostaCorretta;
        this.puntiRispostaSbagliata = puntiRispostaSbagliata;
        this.risposte = risposte;
    }

    public int getId() {
        return id;
    }


    public rispostaDTO addRisposta(rispostaDTO risposta){
        this.risposte.add(risposta);
        return risposta;
    }

    public rispostaDTO removeRispota(rispostaDTO risposta){
        this.risposte.remove(risposta);
        return risposta;
    }

    public ArrayList<rispostaDTO> getRisposte() {
        return risposte;
    }

    public void setRisposte(ArrayList<rispostaDTO> risposte) {
        this.risposte = risposte;
    }

    public String getQuesito() {
        return quesito;
    }

    public void setQuesito(String quesito) {
        this.quesito = quesito;
    }

    public int getPuntiRispostaCorretta() {
        return puntiRispostaCorretta;
    }

    public void setPuntiRispostaCorretta(int puntiRispostaCorretta) {
        this.puntiRispostaCorretta = puntiRispostaCorretta;
    }

    public int getPuntiRispostaSbagliata() {
        return puntiRispostaSbagliata;
    }

    public void setPuntiRispostaSbagliata(int puntiRispostaSbagliata) {
        this.puntiRispostaSbagliata = puntiRispostaSbagliata;
    }
}
