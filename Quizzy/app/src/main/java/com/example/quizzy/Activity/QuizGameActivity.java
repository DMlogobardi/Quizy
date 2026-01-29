package com.example.quizzy.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizzy.DTO.CompletaQuizRequest;
import com.example.quizzy.DTO.CompletaQuizResponse;
import com.example.quizzy.DTO.StartQuizNoPassRequest;
import com.example.quizzy.DTO.domandaDTO;
import com.example.quizzy.DTO.rispostaDTO;
import com.example.quizzy.R;
import com.example.quizzy.Utility.RetrofitInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizGameActivity extends AppCompatActivity {

    private TextView textQuesito;
    private RadioGroup radioGroupRisposte;

    // Dichiarazione dei 3 pulsanti
    private Button btnPrecedente;
    private Button btnProssima;
    private Button btnTermina;

    private int quizId;

    private final List<domandaDTO> listaDomande = new ArrayList<>();
    // Mappa per salvare le risposte: <ID_Domanda, ID_Risposta_Selezionata>
    private final Map<Integer, Integer> risposteDate = new HashMap<>();

    private int index = 0; // Indice della domanda corrente
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game);

        // Recupero dati dall'Intent
        quizId = getIntent().getIntExtra("QUIZ_ID", -1);
        userToken = getIntent().getStringExtra("token");

        if (quizId == -1 || userToken == null) {
            finish();
            return;
        }

        if (!userToken.startsWith("Bearer ")) userToken = "Bearer " + userToken;

        // Binding delle View
        textQuesito = findViewById(R.id.text_quesito);
        radioGroupRisposte = findViewById(R.id.radio_group_risposte);

        // Binding dei 3 pulsanti dal layout XML
        btnPrecedente = findViewById(R.id.button_precedente);
        btnProssima = findViewById(R.id.button_prossima);
        btnTermina = findViewById(R.id.button_termina);

        // Setup Listeners
        btnPrecedente.setOnClickListener(v -> vaiAllaDomandaPrecedente());
        btnProssima.setOnClickListener(v -> vaiAllaDomandaSuccessiva());
        btnTermina.setOnClickListener(v -> confermaTerminaQuiz());

        // Chiamata Server
        caricaDomande(quizId);
    }

    private void caricaDomande(int quizId) {
        RetrofitInstance.getService().startQuiz(userToken, new StartQuizNoPassRequest(quizId))
                .enqueue(new Callback<List<domandaDTO>>() {
                    @Override
                    public void onResponse(Call<List<domandaDTO>> call, Response<List<domandaDTO>> r) {
                        if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                            listaDomande.addAll(r.body());
                            mostraDomanda(); // Mostra la prima domanda
                        } else {
                            Toast.makeText(QuizGameActivity.this, "Errore caricamento o quiz vuoto", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<domandaDTO>> call, Throwable t) {
                        Toast.makeText(QuizGameActivity.this, "Errore di connessione", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void mostraDomanda() {
        if (listaDomande.isEmpty()) return;

        domandaDTO d = listaDomande.get(index);
        textQuesito.setText(d.getQuesito());

        // Pulizia e generazione dinamica RadioButton
        radioGroupRisposte.removeAllViews();
        radioGroupRisposte.clearCheck();

        if (d.getRisposte() != null) {
            for (rispostaDTO r : d.getRisposte()) {
                RadioButton rb = new RadioButton(this);
                rb.setText(r.getAffermazione());
                rb.setId(r.getId()); // Usiamo l'ID della risposta come ID del RadioButton
                rb.setPadding(16, 16, 16, 16);
                radioGroupRisposte.addView(rb);

                // Se l'utente aveva già risposto a questa domanda (navigando indietro e poi avanti), riseleziona la risposta
                if (risposteDate.containsKey(d.getId()) && risposteDate.get(d.getId()) == r.getId()) {
                    rb.setChecked(true);
                }
            }
        }

        // Aggiorna lo stato (abilitato/disabilitato) dei pulsanti
        aggiornaStatoBottoni();
    }

    // Metodo helper per salvare la scelta corrente prima di cambiare pagina
    private void salvaRispostaCorrente() {
        if (listaDomande.isEmpty()) return;

        int selectedId = radioGroupRisposte.getCheckedRadioButtonId();
        domandaDTO domandaCorrente = listaDomande.get(index);

        if (selectedId != -1) {
            // Salva o aggiorna la risposta nella mappa
            risposteDate.put(domandaCorrente.getId(), selectedId);
        } else {
            // Opzionale: se l'utente deseleziona o non risponde, potresti voler rimuovere la risposta precedente
            // risposteDate.remove(domandaCorrente.getId());
        }
    }

    private void vaiAllaDomandaSuccessiva() {
        salvaRispostaCorrente(); // Importante salvare prima di cambiare indice
        if (index < listaDomande.size() - 1) {
            index++;
            mostraDomanda();
        }
    }

    private void vaiAllaDomandaPrecedente() {
        salvaRispostaCorrente(); // Salviamo anche se torna indietro (magari ha cambiato idea)
        if (index > 0) {
            index--;
            mostraDomanda();
        }
    }

    // AGGIUNGI QUESTO METODO
    private void confermaTerminaQuiz() {
        salvaRispostaCorrente(); // Salva l'ultima risposta data, se c'è

        // Mostra un popup di conferma
        new AlertDialog.Builder(this)
                .setTitle("Conferma invio")
                .setMessage("Sei sicuro di voler terminare il quiz? Non potrai più cambiare le risposte.")
                .setPositiveButton("Sì, invia", (dialog, which) -> {
                    // Solo se l'utente dice SÌ, chiamiamo il server
                    inviaRisposteAlServer();
                })
                .setNegativeButton("No", null) // Se dice NO, non facciamo nulla
                .show();
    }

    private void aggiornaStatoBottoni() {
        // Disabilita "Indietro" se siamo alla prima domanda
        btnPrecedente.setEnabled(index > 0);

        // Disabilita "Avanti" se siamo all'ultima domanda
        btnProssima.setEnabled(index < listaDomande.size() - 1);
    }


    // Parte che mostra il punteggio
    private void inviaRisposteAlServer() {
        // 1. Prepariamo la lista complessa richiesta dal JSON
        List<CompletaQuizRequest.RispostaClient> listaRisposteJson = new ArrayList<>();

        // Cicliamo su tutte le domande del quiz
        for (domandaDTO domanda : listaDomande) {

            // Controlliamo se l'utente ha risposto a questa domanda
            if (risposteDate.containsKey(domanda.getId())) {
                int idRispostaScelta = risposteDate.get(domanda.getId());

                // Dobbiamo recuperare l'oggetto rispostaDTO completo per avere "affermazione" e "flag"
                rispostaDTO rispostaOriginale = null;
                for (rispostaDTO r : domanda.getRisposte()) {
                    if (r.getId() == idRispostaScelta) {
                        rispostaOriginale = r;
                        break;
                    }
                }

                // Se abbiamo trovato i dati, creiamo l'oggetto per il JSON
                if (rispostaOriginale != null) {
                    CompletaQuizRequest.RispostaClient rispostaClient = new CompletaQuizRequest.RispostaClient(
                            rispostaOriginale.getId(),          // id risposta
                            domanda.getId(),                    // id domanda
                            rispostaOriginale.getAffermazione(), // testo
                            rispostaOriginale.getFlagRispostaCorretta() // flag boolean
                    );
                    listaRisposteJson.add(rispostaClient);
                }
            }
        }

        // 2. Creiamo la richiesta principale col "quizId" e la lista
        CompletaQuizRequest request = new CompletaQuizRequest(quizId, listaRisposteJson);

        // 3. Chiamata Retrofit
        RetrofitInstance.getService().completaQuiz(userToken, request).enqueue(new Callback<CompletaQuizResponse>() {
            @Override
            public void onResponse(Call<CompletaQuizResponse> call, Response<CompletaQuizResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 200 OK - Mostriamo il punteggio
                    mostraDialogRisultato(response.body().getPunteggio());
                } else {
                    Toast.makeText(QuizGameActivity.this, "Errore invio: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompletaQuizResponse> call, Throwable t) {
                Toast.makeText(QuizGameActivity.this, "Errore di connessione: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostraDialogRisultato(int punteggio) {

        // qui mi viene mosotrato il possibile risultato
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Completato!");
        builder.setMessage("Hai totalizzato: " + punteggio + " punti.");
        builder.setCancelable(false);
        builder.setPositiveButton("Torna alla Home", (dialog, which) -> {
            Intent intent = new Intent(QuizGameActivity.this, HomeActivity.class);
            intent.putExtra("token", userToken);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.show();
    }
}