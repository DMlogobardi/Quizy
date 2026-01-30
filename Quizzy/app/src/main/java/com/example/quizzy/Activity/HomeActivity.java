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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.Adapter.QuizActionListener;
import com.example.quizzy.Adapter.QuizAdapter;
import com.example.quizzy.DTO.DeleteQuizRequest;
import com.example.quizzy.DTO.GetQuizUserRequest;
import com.example.quizzy.DTO.ListQuizDTO;
import com.example.quizzy.DTO.UpRoleResponse;
import com.example.quizzy.MainActivity;
import com.example.quizzy.R;
import com.example.quizzy.Utility.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ImageView menuIcon;
    private LinearLayout menuTendina;
    private TextView btnCambioRuolo;
    private TextView btnCreaQuiz;
    private TextView btnVisualizzaQuiz;
    private TextView btnLogout;
    private RecyclerView listaQuiz;
    private TextView messaggioBenvenuto;
    private Button btnPaginaSuccessiva;
    private QuizAdapter quizAdapter;

    private String userToken;
    private int paginaCorrente = 1;
    private boolean isCreatorMode = false;

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
        btnCreaQuiz = findViewById(R.id.menu_crea_quiz); // Binding nuovo tasto
        btnVisualizzaQuiz = findViewById(R.id.menu_visualizza_quiz);
        btnLogout = findViewById(R.id.menu_logout);
        listaQuiz = findViewById(R.id.lista_quiz_home);
        messaggioBenvenuto = findViewById(R.id.messaggio_benvenuto);
        btnPaginaSuccessiva = findViewById(R.id.button_pagina_successiva);
    }

    private void setupRecyclerView() {
        listaQuiz.setLayoutManager(new LinearLayoutManager(this));

        quizAdapter = new QuizAdapter(new ArrayList<>(), userToken, new QuizActionListener() {
            @Override
            public void onDeleteClick(int idQuiz, int position) {
                if (isCreatorMode) {
                    confermaEliminazione(idQuiz, position);
                } else {
                    Toast.makeText(HomeActivity.this, "Devi essere un Creatore per eliminare i quiz!", Toast.LENGTH_SHORT).show();
                }
            }
        }, isCreatorMode);

        listaQuiz.setAdapter(quizAdapter);
    }

    // Quando clicchi "Diventa Creatore"
    public void eseguiCambioRuolo(View view) {
        if (userToken == null) return;

        RetrofitInstance.getService().upRuolo(userToken).enqueue(new Callback<UpRoleResponse>() {
            @Override
            public void onResponse(Call<UpRoleResponse> call, Response<UpRoleResponse> response) {
                if (response.isSuccessful() && response.body().getToken() != null) {

                    String nuovoJwtRaw = response.body().getToken();
                    userToken = "Bearer " + nuovoJwtRaw;
                    isCreatorMode = true;

                    btnCreaQuiz.setVisibility(View.VISIBLE);
                    btnCambioRuolo.setVisibility(View.GONE);

                    setupRecyclerView();
                    caricaQuiz(true);

                    Toast.makeText(HomeActivity.this, "Ruolo aggiornato! Ora puoi creare ed eliminare.", Toast.LENGTH_LONG).show();


                } else {
                    Toast.makeText(HomeActivity.this, "Errore: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpRoleResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Errore di rete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        menuIcon.setOnClickListener(v -> toggleMenu());

        btnCambioRuolo.setOnClickListener(v -> eseguiCambioRuolo(v));

        btnCreaQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreazioneActivity.class);
            intent.putExtra("token", userToken);
            startActivity(intent);
            toggleMenu();
        });

        btnVisualizzaQuiz.setOnClickListener(v -> {
            paginaCorrente = 1;
            messaggioBenvenuto.setVisibility(View.GONE);
            listaQuiz.setVisibility(View.VISIBLE);
            caricaQuiz(true);
            toggleMenu();
        });

        btnPaginaSuccessiva.setOnClickListener(v -> {
            paginaCorrente++;
            caricaQuiz(false);
        });

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void confermaEliminazione(int idQuiz, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Elimina Quiz")
                .setMessage("Sei sicuro?")
                .setPositiveButton("Elimina", (dialog, which) -> eliminaQuizAPI(idQuiz, position))
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void eliminaQuizAPI(int idQuiz, int position) {
        RetrofitInstance.getService().deleteQuiz(userToken, new DeleteQuizRequest(idQuiz)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    quizAdapter.removeItem(position);
                    Toast.makeText(HomeActivity.this, "Eliminato!", Toast.LENGTH_SHORT).show();
                    if (quizAdapter.getItemCount() == 0) {
                        messaggioBenvenuto.setVisibility(View.VISIBLE);
                        messaggioBenvenuto.setText("Nessun quiz disponibile.");
                        btnPaginaSuccessiva.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Errore server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Errore di rete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void caricaQuiz(boolean pulisciLista) {
        if (userToken == null) {
            Toast.makeText(this, "Token non disponibile", Toast.LENGTH_SHORT).show();
            return;
        }
        GetQuizUserRequest request = new GetQuizUserRequest(paginaCorrente);
        Call<List<ListQuizDTO>> call;
        if (isCreatorMode) {
            call = RetrofitInstance.getService().getQuizListCreator(userToken, request);
        } else {
            call = RetrofitInstance.getService().getQuizListUser(userToken, request);
        }
        call.enqueue(new Callback<List<ListQuizDTO>>() {
            @Override
            public void onResponse(Call<List<ListQuizDTO>> call, Response<List<ListQuizDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (pulisciLista) {
                        quizAdapter.updateData(response.body());
                    } else {
                        quizAdapter.addData(response.body());
                    }
                    if (response.body().isEmpty()) {
                        btnPaginaSuccessiva.setVisibility(View.GONE);
                        // Messaggio differenziato per chiarezza
                        if(pulisciLista) {
                            String msg = isCreatorMode ? "Non hai ancora creato quiz." : "Nessun quiz disponibile.";
                            messaggioBenvenuto.setText(msg);
                            messaggioBenvenuto.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(HomeActivity.this, "Non ci sono altri quiz", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btnPaginaSuccessiva.setVisibility(View.VISIBLE);
                        messaggioBenvenuto.setVisibility(View.GONE);
                    }
                } else {
                    // Se qui ricevi ancora errore, stampiamo il codice per debug
                    Toast.makeText(HomeActivity.this, "Errore Server: " + response.code(), Toast.LENGTH_SHORT).show();
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





    /*
    private void caricaQuiz(boolean pulisciLista) {
        GetQuizUserRequest request = new GetQuizUserRequest(paginaCorrente);
        RetrofitInstance.getService().getQuizListUser(userToken, request).enqueue(new Callback<List<ListQuizDTO>>() {
            @Override
            public void onResponse(Call<List<ListQuizDTO>> call, Response<List<ListQuizDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (pulisciLista) quizAdapter.updateData(response.body());
                    else quizAdapter.addData(response.body());

                    if (!response.body().isEmpty()) btnPaginaSuccessiva.setVisibility(View.VISIBLE);
                    else btnPaginaSuccessiva.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<List<ListQuizDTO>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Errore caricamento", Toast.LENGTH_SHORT).show();
            }
        });
    }
    */


    public void toggleMenu() {
        if (menuTendina.getVisibility() == View.VISIBLE) {
            menuTendina.setVisibility(View.GONE);
        } else {
            menuTendina.setVisibility(View.VISIBLE);
            menuTendina.bringToFront();
        }
    }

    // LogOut
    private void performLogout() {
        // 1. Controlla se abbiamo il token
        if (userToken == null) {
            // Se non c'è token, pulisci tutto ed esci direttamente
            clearLocalDataAndExit();
            return;
        }

        // 2. Chiama l'endpoint di Logout del server
        // Nota: userToken deve già contenere "Bearer " + stringa, o glielo aggiungi qui
        // Se userToken è solo la stringa raw, usa: "Bearer " + userToken
        RetrofitInstance.getService().logout(userToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Che il server risponda 200 o errore, noi eseguiamo comunque il logout lato app
                clearLocalDataAndExit();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Anche se manca la connessione, l'utente deve poter uscire dall'app
                clearLocalDataAndExit();
            }
        });
    }

    // Metodo di supporto per pulire e chiudere
    private void clearLocalDataAndExit() {
        // 1. Cancella le SharedPreferences
        getSharedPreferences("QuizzyPrefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // 2. Resetta variabile locale
        userToken = null;

        // 3. Vai alla Login
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}