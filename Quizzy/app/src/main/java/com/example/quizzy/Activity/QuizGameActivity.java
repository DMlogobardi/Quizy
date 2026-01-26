package com.example.quizzy.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private Button btnAzione; // Unico bottone che cambia testo (Prossima/Termina)

    private final List<domandaDTO> listaDomande = new ArrayList<>();
    private final Map<Integer, Integer> risposteDate = new HashMap<>();
    private int index = 0;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game);

        int quizId = getIntent().getIntExtra("QUIZ_ID", -1);
        userToken = getIntent().getStringExtra("token");

        // Unico controllo di sicurezza essenziale: se mancano dati, chiudi.
        if (quizId == -1 || userToken == null) {
            finish(); return;
        }

        // Normalizzazione token rapida
        if (!userToken.startsWith("Bearer ")) userToken = "Bearer " + userToken;

        // Bind diretto senza metodo dedicato
        textQuesito = findViewById(R.id.text_quesito);
        radioGroupRisposte = findViewById(R.id.radio_group_risposte);
        btnAzione = findViewById(R.id.button_prossima);
        // Nota: Assicurati che nel layout XML ci sia un solo bottone principale o nascondi quello 'Termina'
        // Se usi due bottoni distinti come prima, puoi lasciarli, qui ho semplificato la logica su uno solo.

        // Listener compatto
        btnAzione.setOnClickListener(v -> gestisciClickAzione());
        findViewById(R.id.button_termina).setOnClickListener(v -> chiudiQuiz()); // Se presente nell'XML

        // Chiamata Server diretta
        RetrofitInstance.getService().startQuiz(userToken, new StartQuizNoPassRequest(quizId))
                .enqueue(new Callback<List<domandaDTO>>() {
                    @Override
                    public void onResponse(Call<List<domandaDTO>> call, Response<List<domandaDTO>> r) {
                        if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                            listaDomande.addAll(r.body());
                            mostraDomanda();
                        } else {
                            Toast.makeText(QuizGameActivity.this, "Errore caricamento", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<domandaDTO>> call, Throwable t) {
                        finish(); // Fallimento rete -> chiusura diretta
                    }
                });
    }

    private void mostraDomanda() {
        domandaDTO d = listaDomande.get(index);
        textQuesito.setText(d.getQuesito());

        // Logica testo bottone
        btnAzione.setText(index == listaDomande.size() - 1 ? "Completa" : "Prossima");

        radioGroupRisposte.removeAllViews();
        radioGroupRisposte.clearCheck();

        if (d.getRisposte() != null) {
            for (rispostaDTO r : d.getRisposte()) {
                RadioButton rb = new RadioButton(this);
                rb.setText(r.getAffermazione());
                rb.setId(r.getId());
                rb.setPadding(16, 16, 16, 16);
                radioGroupRisposte.addView(rb);

                // Ripristino selezione
                if (risposteDate.containsKey(d.getId()) && risposteDate.get(d.getId()) == r.getId()) {
                    rb.setChecked(true);
                }
            }
        }
    }

    private void gestisciClickAzione() {
        // Salva risposta corrente
        int selected = radioGroupRisposte.getCheckedRadioButtonId();
        if (selected != -1) risposteDate.put(listaDomande.get(index).getId(), selected);

        if (index < listaDomande.size() - 1) {
            index++;
            mostraDomanda();
        } else {
            chiudiQuiz();
        }
    }

    private void chiudiQuiz() {
        // Qui invierai i dati. Per ora basta chiudere.
        Toast.makeText(this, "Risposte salvate: " + risposteDate.size(), Toast.LENGTH_SHORT).show();
        finish();
    }
}