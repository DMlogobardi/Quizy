package com.example.quizzy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

        String userStr = username.getText().toString().trim();
        String passStr = password.getText().toString().trim();

        if (userStr.isEmpty() || passStr.isEmpty()) {
            Toast.makeText(ctx, "Inserire le credenziali per procedere", Toast.LENGTH_SHORT).show();
            return; // IMPORTANTE: interrompe il metodo e non invia la richiesta al server
        }
        LoginRequest request = new LoginRequest(username.getText().toString().trim(), password.getText().toString().trim());

        RetrofitInstance.getService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String token = "Bearer " + response.body().getToken();
                    System.out.println(token);

                    Toast.makeText(ctx, "Benvenuto", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ctx, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("token", token);
                    startActivity(intent);


                    System.out.println("Login avvenuto con successo");
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

