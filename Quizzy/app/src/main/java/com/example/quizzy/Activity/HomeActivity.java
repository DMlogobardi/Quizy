package com.example.quizzy.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.Adapter.QuizAdapter;
import com.example.quizzy.DTO.GetQuizUserRequest;
import com.example.quizzy.DTO.ListQuizDTO;
import com.example.quizzy.DTO.UpRoleResponse;
import com.example.quizzy.R;
import com.example.quizzy.Utility.RetrofitInstance;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ImageView menuIcon;
    private LinearLayout menuTendina;
    private TextView btnCambioRuolo;
    private TextView btnVisualizzaQuiz;
    private RecyclerView listaQuiz;
    private TextView messaggioBenvenuto;
    private Button btnPaginaSuccessiva;
    private QuizAdapter quizAdapter;

    private String userToken;
    private int paginaCorrente = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userToken = getIntent().getStringExtra("token");

        bindViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void bindViews() {
        menuIcon = findViewById(R.id.menu_icon);
        menuTendina = findViewById(R.id.menu_tendina);
        btnCambioRuolo = findViewById(R.id.menu_cambio_ruolo);
        btnVisualizzaQuiz = findViewById(R.id.menu_visualizza_quiz);
        listaQuiz = findViewById(R.id.lista_quiz_home);
        messaggioBenvenuto = findViewById(R.id.messaggio_benvenuto);
        btnPaginaSuccessiva = findViewById(R.id.button_pagina_successiva);
    }

    // Qui viene passato il Token alla QuizAdapter
    private void setupRecyclerView() {
        listaQuiz.setLayoutManager(new LinearLayoutManager(this));
        quizAdapter = new QuizAdapter(new ArrayList<>(), userToken);
        listaQuiz.setAdapter(quizAdapter);
    }

    private void setupClickListeners() {
        menuIcon.setOnClickListener(v -> toggleMenu());

        btnCambioRuolo.setOnClickListener(v -> {
            eseguiCambioRuolo(v);
            toggleMenu();
        });

        btnVisualizzaQuiz.setOnClickListener(v -> {
            paginaCorrente = 1; // Reset to first page
            messaggioBenvenuto.setVisibility(View.GONE);
            listaQuiz.setVisibility(View.VISIBLE);
            caricaQuiz(true); // First load, so clear existing data
            toggleMenu();
        });

        btnPaginaSuccessiva.setOnClickListener(v -> {
            paginaCorrente++;
            caricaQuiz(false); // Subsequent load, so append data
        });
    }

    private void caricaQuiz(boolean pulisciLista) {
        if (userToken == null) {
            Toast.makeText(this, "Token non disponibile", Toast.LENGTH_SHORT).show();
            return;
        }

        GetQuizUserRequest request = new GetQuizUserRequest(paginaCorrente);
        RetrofitInstance.getService().getQuizListUser(userToken, request).enqueue(new Callback<List<ListQuizDTO>>() {
            @Override
            public void onResponse(Call<List<ListQuizDTO>> call, Response<List<ListQuizDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (pulisciLista) {
                        quizAdapter.updateData(response.body());
                    } else {
                        quizAdapter.addData(response.body());
                    }

                    // Show or hide the "next page" button based on the response
                    if (response.body().isEmpty()) {
                        btnPaginaSuccessiva.setVisibility(View.GONE);
                        Toast.makeText(HomeActivity.this, "Non ci sono altri quiz", Toast.LENGTH_SHORT).show();
                    } else {
                        btnPaginaSuccessiva.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Errore: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnPaginaSuccessiva.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<ListQuizDTO>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Errore di rete: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnPaginaSuccessiva.setVisibility(View.GONE);
            }
        });
    }

    // metodo per diventare il creatore di tutti i cieli della terra
    public void eseguiCambioRuolo(View view) {
        Context ctx = HomeActivity.this;

        if (userToken == null) {
            Toast.makeText(ctx, "Token non disponibile, impossibile cambiare ruolo", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitInstance.getService().upRuolo(userToken).enqueue(new Callback<UpRoleResponse>() {
            @Override
            public void onResponse(Call<UpRoleResponse> call, Response<UpRoleResponse> response) {
                if (response.isSuccessful() && response.body().getToken() != null) {
                    try {



                        String nuovoJwtRaw = response.body().getToken();
                        userToken = "Bearer " + nuovoJwtRaw;

                        Intent intent = new Intent(ctx, CreazioneActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("token", userToken);
                        startActivity(intent);

                        Toast.makeText(HomeActivity.this, "Ruolo Aggiornato! Ora sei un Creatore.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this, "Errore lettura dati", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Fallito: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpRoleResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Errore di rete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void toggleMenu() {
        if (menuTendina.getVisibility() == View.VISIBLE) {
            menuTendina.setVisibility(View.GONE);
        } else {
            menuTendina.setVisibility(View.VISIBLE);
            menuTendina.bringToFront();
        }
    }
}
