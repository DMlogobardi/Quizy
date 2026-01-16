package com.example.quizy.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.quizy.R;
import com.example.quizy.models.RichiesteRegistrazione;
import com.example.quizy.controllers.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrazioneUtente extends AppCompatActivity {

    private EditText mNome;
    private EditText mCognome;
    private EditText mData;
    private EditText mUsername;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        mNome = findViewById(R.id.et_nome);
        mCognome = findViewById(R.id.et_cognome);
        mData = findViewById(R.id.et_data_nascita);
        mUsername = findViewById(R.id.et_new_username);
        mPassword = findViewById(R.id.et_new_password);
    }

    // Collega questo metodo all'attributo onClick del bottone "Registrati" nel layout XML
    public void onRegistraClick(View view) {
        registrazioneUtente();
    }

    public void registrazioneUtente() {
        // 1. Estrazione dei dati dagli EditText
        String nome = mNome.getText().toString().trim();
        String cognome = mCognome.getText().toString().trim();
        String data = mData.getText().toString().trim();
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        // 2. Validazione di base (Controllo campi vuoti)
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(cognome) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Creazione dell'oggetto dati
        RichiesteRegistrazione nuovoUtente = new RichiesteRegistrazione(nome, cognome, data, username, password);

        // 4. Chiamata Retrofit
        Call<Void> call = RetrofitClient.getService().registraUtente(nuovoUtente);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Successo (Codice 200-299)
                    Toast.makeText(RegistrazioneUtente.this, "Registrazione avvenuta!", Toast.LENGTH_LONG).show();

                    // Vai alla Home o alla Login
                    ritornaHome2(null);
                } else {
                    // Errore dal server (es. Username già esistente, Codice 400 o 500)
                    Toast.makeText(RegistrazioneUtente.this, "Errore: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Errore di connessione (Server spento, no internet, ecc.)
                Toast.makeText(RegistrazioneUtente.this, "Errore connessione: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void ritornaHome2(View view) {
        Intent i = new Intent(RegistrazioneUtente.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}