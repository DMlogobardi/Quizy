package com.example.quizzy.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizzy.DTO.LoginResponse;
import com.example.quizzy.DTO.RegistrazioneRequest;
import com.example.quizzy.MainActivity;
import com.example.quizzy.R;
import com.example.quizzy.Utility.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegistrazioneActivity extends AppCompatActivity {

    private EditText nome;
    private EditText cognome;
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        nome = findViewById(R.id.et_nome);
        cognome= findViewById(R.id.et_cognome);
        username= findViewById(R.id.et_new_username);
        password= findViewById(R.id.et_new_password);
    }

    public void ritornaHome2(View view){
        Context ctx = RegistrazioneActivity.this;

        RegistrazioneRequest richiesta = new RegistrazioneRequest(nome.getText().toString().trim(), username.getText().toString().trim(), password.getText().toString().trim(), cognome.getText().toString().trim());
        RetrofitInstance.getService().registrazione(richiesta).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response){

                if(response.isSuccessful() ){

                    // passo activity
                    Intent intent = new Intent(ctx, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(ctx, "registrazione errata", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ctx, "Errore di rete: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });


    }

}
