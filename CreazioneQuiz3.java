package com.example.quizy.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizy.R;

public class CreazioneQuiz3 extends AppCompatActivity {

    private TextView salvaTermina;
    private TextView btnSalvaTermina;
    private TextView btnNuovaDomanda;
    private TextView tvCounterDomande;

    private EditText etDomanda;
    private EditText etRisposta1;
    private EditText etRisposta2;
    private EditText etRisposta3;

    // 2. Variabile per tenere il conto delle domande (inizia da 0)
    private int contatoreDomande = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_quiz3);

        salvaTermina = findViewById(R.id.salva_termina);
    }

    public void ritornaHome (View view){
        Intent i = new Intent(CreazioneQuiz3.this, Home.class);

        // elimino le precedenti activiti e ritorna alla home senza ricaricarla
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(i);
    }
}
