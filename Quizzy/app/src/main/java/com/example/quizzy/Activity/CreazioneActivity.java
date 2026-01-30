package com.example.quizzy.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.Adapter.DomandeAdapter;
import com.example.quizzy.DTO.QuizDTO;
import com.example.quizzy.DTO.domandaDTO;
import com.example.quizzy.DTO.rispostaDTO;
import com.example.quizzy.MainActivity;
import com.example.quizzy.R;
import com.example.quizzy.Utility.RetrofitInstance;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreazioneActivity extends AppCompatActivity {

    private TextInputEditText editTextTitolo, editTextDescrizione, editTextDifficolta, editTextTempo, editTextPassword;
    private RecyclerView recyclerViewDomande;
    private DomandeAdapter domandeAdapter;
    private final ArrayList<domandaDTO> domandeList = new ArrayList<>();

    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_quiz);
        token = getIntent().getStringExtra("token");

        bindViews();
        setupRecyclerView();

        Button buttonAggiungiDomanda = findViewById(R.id.button_aggiungi_domanda);
        buttonAggiungiDomanda.setOnClickListener(v -> aggiungiDomanda());

        Button buttonSalvaQuiz = findViewById(R.id.button_salva_quiz);
        buttonSalvaQuiz.setOnClickListener(v -> salvaQuiz());
    }

    private void bindViews() {
        editTextTitolo = findViewById(R.id.edit_text_titolo_quiz);
        editTextDescrizione = findViewById(R.id.edit_text_descrizione_quiz);
        editTextDifficolta = findViewById(R.id.edit_text_difficolta_quiz);
        editTextTempo = findViewById(R.id.edit_text_tempo_quiz);
        editTextPassword = findViewById(R.id.edit_text_password_quiz);
        recyclerViewDomande = findViewById(R.id.recycler_view_domande);
    }

    private void setupRecyclerView() {
        domandeAdapter = new DomandeAdapter(domandeList);
        recyclerViewDomande.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDomande.setAdapter(domandeAdapter);
    }

    private void aggiungiDomanda() {
        ArrayList<rispostaDTO> risposte = new ArrayList<>();
        risposte.add(new rispostaDTO("", false)); // Add a default empty answer
        domandeList.add(new domandaDTO("", 0, 0, risposte));
        domandeAdapter.notifyItemInserted(domandeList.size() - 1);
    }

    private void salvaQuiz() {
        String titolo = editTextTitolo.getText().toString().trim();
        String descrizione = editTextDescrizione.getText().toString().trim();
        String difficolta = editTextDifficolta.getText().toString().trim();
        String tempo = editTextTempo.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        int numeroDomande = domandeList.size();

        if (titolo.isEmpty() || descrizione.isEmpty() || difficolta.isEmpty() || tempo.isEmpty() || domandeList.isEmpty()) {
            Toast.makeText(this, "Per favore, compila tutti i campi del quiz e aggiungi almeno una domanda.", Toast.LENGTH_SHORT).show();
            return;
        }

        QuizDTO quiz = new QuizDTO(titolo, descrizione, difficolta, tempo, numeroDomande, password, domandeList);
        sendQuizToServer(quiz);
    }

    private void sendQuizToServer(QuizDTO quiz) {
        Context ctx = CreazioneActivity.this;

        RetrofitInstance.getService().createQuiz(token, quiz).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreazioneActivity.this, "Quiz creato con successo!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ctx, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("token", token);
                    startActivity(intent);

                    finish();

                } else {
                    System.out.println(token);
                    Toast.makeText(CreazioneActivity.this, "Errore nel salvare il quiz. Codice: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreazioneActivity.this, "Errore di rete: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
