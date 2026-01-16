package com.example.quizy.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizy.R;


public class CreazioneQuiz2 extends AppCompatActivity {

    private TextView link2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_quiz2);
        link2 = findViewById(R.id.link_continua);
    }

    public void successivaPagina2(View view){
        Intent i = new Intent(getApplicationContext(), CreazioneQuiz3.class);
        startActivity(i);
    }
}
