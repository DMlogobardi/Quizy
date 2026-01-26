package com.example.quizzy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizzy.Activity.HomeActivity;
import com.example.quizzy.Activity.RegistrazioneActivity;
import com.example.quizzy.DTO.LoginRequest;
import com.example.quizzy.DTO.LoginResponse;
import com.example.quizzy.Utility.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
    }

    public void performLogin(View view) {
        Context ctx = MainActivity.this;

        LoginRequest request = new LoginRequest(username.getText().toString().trim(), password.getText().toString().trim());

        //collegamento al server
        RetrofitInstance.getService().login(request).enqueue(new Callback<LoginResponse>() {
                                    //registrazion(token, request)
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //login riuscito altrimenti, else: login fallito
                    String token = "Bearer " + response.body().getToken();
                    System.out.println(token);


                    // Qui robbe devi mandare l'utente alla home che dovrai fare quando la fai passandogli ovviamente
                    // anche il token, il token dovrai portartelo dietro ad ogni activity che lancerai
                    // visto che non usi un sessionManager


                    Intent intent = new Intent(ctx, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("token", token);
                    startActivity(intent);


                    //La parte degli intent per ora te la commento quando avrai l'activity basta che cambi nome nell'intent
                    // Il flag che ti ho messo serve per pulire tutte le activity precedenti mentre se vuoi semplicemente che
                    // non venga messa nel backstack basta chiamare finish() dopo l'intent
                    // Per ora ti stampo semplicemente un messaggio che ti dice se il login è avvenuto con successo
                    System.out.println("Login avvenuto con successo");
                    // Non ti dimenticare di aggiungere l'activity al manifest.xml, ovviamente anche tutte le altre activity
                } else {
                    Toast.makeText(ctx, "Credenziali errate", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(ctx, "Errore di rete: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void registrazioneActivity(View view){
        Context ctx = MainActivity.this;

        Intent intent = new Intent(ctx, RegistrazioneActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}

