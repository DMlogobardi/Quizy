package com.example.quizzy.Activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.Adapter.DomandeAdapter;
import com.example.quizzy.DTO.StartQuizNoPassRequest;
import com.example.quizzy.DTO.UpdateQuizRequest;
import com.example.quizzy.DTO.domandaDTO;
import com.example.quizzy.DTO.rispostaDTO;
import com.example.quizzy.R;
import com.example.quizzy.Utility.RetrofitInstance;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditQuizActivity extends AppCompatActivity {

    private TextInputEditText editTextTitolo, editTextDescrizione, editTextTempo, editTextPassword;
    private Spinner spinnerDifficolta;
    private RecyclerView recyclerViewDomande;
    private DomandeAdapter domandeAdapter;
    private List<domandaDTO> domandeList = new ArrayList<>();
    private Button buttonSalvaModifiche, buttonAggiungiDomanda;

    private String token;
    private int quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quiz);

        // 1. Recupero ID e Token
        quizId = getIntent().getIntExtra("QUIZ_ID", -1);
        token = getIntent().getStringExtra("token");

        if (quizId == -1 || token == null) {
            Toast.makeText(this, "Dati del quiz mancanti", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupSpinner();
        setupRecyclerView();

        popolaDatiBaseDallIntent();
        caricaDomandeDalServer();

        // Listener per aggiungere una nuova domanda vuota
        buttonAggiungiDomanda.setOnClickListener(v -> {
            ArrayList<rispostaDTO> r = new ArrayList<>();
            r.add(new rispostaDTO("", false));
            domandeList.add(new domandaDTO("", 0, 0, r));
            domandeAdapter.notifyItemInserted(domandeList.size() - 1);
        });
        buttonSalvaModifiche.setOnClickListener(v -> salvaModificheQuiz());
    }
    private void bindViews() {
        editTextTitolo = findViewById(R.id.edit_text_titolo);
        editTextDescrizione = findViewById(R.id.edit_text_descrizione);
        editTextTempo = findViewById(R.id.edit_text_tempo);
        editTextPassword = findViewById(R.id.edit_text_password_quiz_modifica);
        spinnerDifficolta = findViewById(R.id.spinner_difficolta);
        recyclerViewDomande = findViewById(R.id.recycler_view_domande_modifica);
        buttonSalvaModifiche = findViewById(R.id.button_salva_modifiche);
        buttonAggiungiDomanda = findViewById(R.id.button_aggiungi_domanda_modifica);
    }
    private void popolaDatiBaseDallIntent() {
        // Questi dati arrivano dal QuizAdapter (ListQuizDTO li ha)
        editTextTitolo.setText(getIntent().getStringExtra("TITOLO"));
        editTextDescrizione.setText(getIntent().getStringExtra("DESCRIZIONE"));
        editTextTempo.setText(getIntent().getStringExtra("TEMPO"));

        String diff = getIntent().getStringExtra("DIFFICOLTA");
        ArrayAdapter adapter = (ArrayAdapter) spinnerDifficolta.getAdapter();
        if (diff != null) {
            int pos = adapter.getPosition(diff);
            spinnerDifficolta.setSelection(pos);
        }
    }
    private void caricaDomandeDalServer() {
        // Usiamo startQuiz perché getQuizListCreator non ci dà le domande
        RetrofitInstance.getService().startQuiz(token, new StartQuizNoPassRequest(quizId))
                .enqueue(new Callback<List<domandaDTO>>() {
                    @Override
                    public void onResponse(Call<List<domandaDTO>> call, Response<List<domandaDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            domandeList.clear();
                            domandeList.addAll(response.body());
                            domandeAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(EditQuizActivity.this, "Impossibile recuperare le domande", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<domandaDTO>> call, Throwable t) {
                        Toast.makeText(EditQuizActivity.this, "Errore di rete: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void salvaModificheQuiz() {
        // Raccolgo i dati aggiornati dall'interfaccia
        String titolo = editTextTitolo.getText().toString().trim();
        String descrizione = editTextDescrizione.getText().toString().trim();
        String tempo = editTextTempo.getText().toString().trim();
        String difficolta = spinnerDifficolta.getSelectedItem().toString();
        String password = editTextPassword.getText().toString().trim();

        if (titolo.isEmpty() || domandeList.isEmpty()) {
            Toast.makeText(this, "Compila i campi obbligatori", Toast.LENGTH_SHORT).show();
            return;
        }
        UpdateQuizRequest request = new UpdateQuizRequest(
                quizId,
                titolo,
                descrizione,
                difficolta,
                tempo,
                domandeList.size(),
                password,
                (ArrayList<domandaDTO>) domandeList
        );

        RetrofitInstance.getService().updateQuiz(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditQuizActivity.this, "Quiz aggiornato con successo!", Toast.LENGTH_SHORT).show();
                    finish(); // Torna alla Home
                } else {
                    Toast.makeText(EditQuizActivity.this, "Errore salvataggio: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditQuizActivity.this, "Errore di connessione", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficolta_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficolta.setAdapter(adapter);
    }
    private void setupRecyclerView() {
        domandeAdapter = new DomandeAdapter(domandeList);
        recyclerViewDomande.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDomande.setAdapter(domandeAdapter);
    }
}