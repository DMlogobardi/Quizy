package com.example.quizzy.DTO;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CompletaQuizRequest {

    // Struttura: "quiz": { "id": 2 }
    @SerializedName("quiz")
    private QuizIdWrapper quiz;

    // Struttura: "risposteClient": [ ... ]
    @SerializedName("risposteClient")
    private List<RispostaClient> risposteClient;

    public CompletaQuizRequest(int quizId, List<RispostaClient> risposteClient) {
        this.quiz = new QuizIdWrapper(quizId);
        this.risposteClient = risposteClient;
    }

    // --- Classi Interne per la struttura JSON ---

    public static class QuizIdWrapper {
        @SerializedName("id")
        private int id;

        public QuizIdWrapper(int id) {
            this.id = id;
        }
    }

    public static class RispostaClient {
        @SerializedName("id")
        private int id; // ID della risposta

        @SerializedName("domanda")
        private DomandaIdWrapper domanda;

        @SerializedName("affermazione")
        private String affermazione;

        @SerializedName("flagRispostaCorretta")
        private boolean flagRispostaCorretta;

        public RispostaClient(int idRisposta, int idDomanda, String affermazione, boolean flagRispostaCorretta) {
            this.id = idRisposta;
            this.domanda = new DomandaIdWrapper(idDomanda);
            this.affermazione = affermazione;
            this.flagRispostaCorretta = flagRispostaCorretta;
        }
    }

    public static class DomandaIdWrapper {
        @SerializedName("id")
        private int id;

        public DomandaIdWrapper(int id) {
            this.id = id;
        }
    }
}