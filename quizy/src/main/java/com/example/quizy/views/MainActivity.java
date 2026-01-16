package com.example.quizy.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizy.R;
import com.example.quizy.controllers.RetrofitClient;
import com.example.quizy.controllers.UserManager;
import com.example.quizy.models.LoginResponse; // <--- Importante
import com.example.quizy.models.RichiesteLogin; // <--- Importante

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private UserManager userManager;
    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userManager = new UserManager();
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
    }

    public void homeActivity(View view) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // 1. VALIDAZIONE LOCALE (UserManager)
        // Controllo se i campi sono scritti bene prima di chiamare il server
        if (!userManager.validazioneUsername(username)) {
            usernameInput.setError("Inserisci username");
            return;
        }
        if (!userManager.validazionePassword(password)) {
            passwordInput.setError("Password troppo corta o insicura");
            return;
        }

        // 2. CHIAMATA AL SERVER (Retrofit)
        RichiesteLogin loginData = new RichiesteLogin(username, password);

        // Nota: ora ci aspettiamo una LoginResponse, non Void
        Call<LoginResponse> call = RetrofitClient.getService().login(loginData);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String token = response.body().getToken();

                    Toast.makeText(MainActivity.this, "Login OK!", Toast.LENGTH_SHORT).show();

                    // Passiamo alla Home portandoci dietro il token e il nome
                    Intent i = new Intent(getApplicationContext(), Home.class);
                    i.putExtra("NOME_UTENTE", username);
                    i.putExtra("USER_TOKEN", token);
                    startActivity(i);
                    finish();
                } else {
                    // ERRORE DAL SERVER (Codice 401 Unauthorized)
                    Toast.makeText(MainActivity.this, "Credenziali errate", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // ERRORE DI CONNESSIONE (Server spento, IP sbagliato)
                Toast.makeText(MainActivity.this, "Errore connessione: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void registrazioneActivity(View view) {
        Intent i = new Intent(getApplicationContext(), RegistrazioneUtente.class);
        startActivity(i);
    }
}


/*

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
    private EditText passwordInput;

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
        passwordInput = findViewById(R.id.password);

    }

    // controllo dello username valido
    public void homeActivity (View view){

        //prendi l'input dell'utente e lo converto in stringa
        String usernameInserito= usernameInput.getText().toString().trim();
        String passwordInserita= passwordInput.getText().toString().trim();

        if(user.autenticazioneLogin(usernameInserito,  passwordInserita)){
            Intent i = new Intent(getApplicationContext(), Home.class);
            startActivity(i);

            finish();
        } else {
            Toast.makeText(
                    this,
                    "Username o Password errata",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void registrazioneActivity (View view){
        Intent i = new Intent(getApplicationContext(), RegistrazioneUtente.class);
        startActivity(i);
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


 */

