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
    private int index = 0;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game);
        String passwordRicevuta = getIntent().getStringExtra("QUIZ_PASSWORD");

        quizId = getIntent().getIntExtra("QUIZ_ID", -1);
        userToken = getIntent().getStringExtra("token");

        if (quizId == -1 || userToken == null) {
            finish();
            return;
        }

        if (!userToken.startsWith("Bearer ")) userToken = "Bearer " + userToken;

        textQuesito = findViewById(R.id.text_quesito);
        radioGroupRisposte = findViewById(R.id.radio_group_risposte);

        btnPrecedente = findViewById(R.id.button_precedente);
        btnProssima = findViewById(R.id.button_prossima);
        btnTermina = findViewById(R.id.button_termina);

        btnPrecedente.setOnClickListener(v -> vaiAllaDomandaPrecedente());
        btnProssima.setOnClickListener(v -> vaiAllaDomandaSuccessiva());
        btnTermina.setOnClickListener(v -> confermaTerminaQuiz());

        caricaDomande(quizId);
    }

    private void caricaDomande(int quizId) {
        String passwordRicevuta = getIntent().getStringExtra("QUIZ_PASSWORD");
        Call<List<domandaDTO>> call;

        if (passwordRicevuta != null && !passwordRicevuta.isEmpty()) {
            // Usa l'endpoint con password
            call = RetrofitInstance.getService().starQuizPassword(userToken, new StartQuizNoPassRequest(quizId, passwordRicevuta));
        } else {
            // Usa l'endpoint senza password
            call = RetrofitInstance.getService().startQuiz(userToken, new StartQuizNoPassRequest(quizId));
        }

        call.enqueue(new Callback<List<domandaDTO>>() {
            @Override
            public void onResponse(Call<List<domandaDTO>> call, Response<List<domandaDTO>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    listaDomande.addAll(r.body());
                    mostraDomanda();
                } else {
                    Toast.makeText(QuizGameActivity.this, "Password errata o errore caricamento", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<List<domandaDTO>> call, Throwable t) {
                finish();
            }
        });
    }

    private void mostraDomanda() {
        if (listaDomande.isEmpty()) return;

        domandaDTO d = listaDomande.get(index);
        textQuesito.setText(d.getQuesito());

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

        aggiornaStatoBottoni();
    }

    private void salvaRispostaCorrente() {
        if (listaDomande.isEmpty()) return;

        int selectedId = radioGroupRisposte.getCheckedRadioButtonId();
        domandaDTO domandaCorrente = listaDomande.get(index);

        if (selectedId != -1) {
            risposteDate.put(domandaCorrente.getId(), selectedId);
        } else {
            // in futuro se l'utente deseleziona o non risponde, potresti voler rimuovere la risposta precedente
            // risposteDate.remove(domandaCorrente.getId());
        }
    }

    private void vaiAllaDomandaSuccessiva() {
        salvaRispostaCorrente();
        if (index < listaDomande.size() - 1) {
            index++;
            mostraDomanda();
        }
    }

    private void vaiAllaDomandaPrecedente() {
        salvaRispostaCorrente();
        if (index > 0) {
            index--;
            mostraDomanda();
        }
    }

    private void confermaTerminaQuiz() {
        salvaRispostaCorrente();

        new AlertDialog.Builder(this)
                .setTitle("Conferma invio")
                .setMessage("Sei sicuro di voler terminare il quiz? Non potrai più cambiare le risposte.")
                .setPositiveButton("Sì, invia", (dialog, which) -> {
                    inviaRisposteAlServer();
                })
                .setNegativeButton("No", null)
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
        List<CompletaQuizRequest.RispostaClient> listaRisposteJson = new ArrayList<>();

        for (domandaDTO domanda : listaDomande) {

            if (risposteDate.containsKey(domanda.getId())) {
                int idRispostaScelta = risposteDate.get(domanda.getId());

                rispostaDTO rispostaOriginale = null;
                for (rispostaDTO r : domanda.getRisposte()) {
                    if (r.getId() == idRispostaScelta) {
                        rispostaOriginale = r;
                        break;
                    }
                }
                if (rispostaOriginale != null) {
                    CompletaQuizRequest.RispostaClient rispostaClient = new CompletaQuizRequest.RispostaClient(
                            rispostaOriginale.getId(),
                            domanda.getId(),
                            rispostaOriginale.getAffermazione(),
                            rispostaOriginale.getFlagRispostaCorretta()
                    );
                    listaRisposteJson.add(rispostaClient);
                }
            }
        }

        CompletaQuizRequest request = new CompletaQuizRequest(quizId, listaRisposteJson);

        RetrofitInstance.getService().completaQuiz(userToken, request).enqueue(new Callback<CompletaQuizResponse>() {
            @Override
            public void onResponse(Call<CompletaQuizResponse> call, Response<CompletaQuizResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
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
            Toast.makeText(QuizGameActivity.this, "Quiz Completato!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(QuizGameActivity.this, HomeActivity.class);
            intent.putExtra("token", userToken);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.show();
    }
}