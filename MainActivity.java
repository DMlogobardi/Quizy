package com.example.quizy.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quizy.controllers.UserManager;

import com.example.quizy.R;

public class MainActivity extends AppCompatActivity {


    private UserManager user;
    private EditText usernameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = new UserManager();
        usernameInput = findViewById(R.id.username);

    }

    // controllo dello username valido
    public void cambioActivity (View view){

        //prendi l'input dell'utente e lo converto in stringa
        String usernameInserito= usernameInput.getText().toString().trim();

        if(user.autenticazioneLogin(usernameInserito)){
            Intent i = new Intent(getApplicationContext(), Home.class);
            startActivity(i);

            finish();
        } else {
            Toast.makeText(
                    this,
                    "Username non valido",
                    Toast.LENGTH_SHORT
            ).show();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

