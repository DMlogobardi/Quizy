package com.example.quizy.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizy.R;



public class CreazioneQuiz1 extends AppCompatActivity {

    private Button buttonCreaQuiz;
    private ImageView tornaIndietro;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_quiz1);

        buttonCreaQuiz = findViewById(R.id.btn_crea_quiz);
        tornaIndietro = findViewById(R.id.btn_back);

    }

    public void successivaPagina (View view){
        Intent i = new Intent(getApplicationContext(), CreazioneQuiz2.class);
        startActivity(i);
    }

    public void precedentePagina (View view){
        Intent i = new Intent(CreazioneQuiz1.this, Home.class);
        startActivity(i);
    }

}
