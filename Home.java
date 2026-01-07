package com.example.quizy.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizy.R;

public class Home extends AppCompatActivity {

    private ImageView menuIcon;
    private LinearLayout menuTendina;
    private TextView btnCambioRuolo;
    private TextView btnVisualizzaQuiz;

    private RecyclerView listaQuizHome;
    private TextView messaggioBenvenuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        menuIcon = findViewById(R.id.menu_icon);
        menuTendina = findViewById(R.id.menu_tendina);
        btnCambioRuolo = findViewById(R.id.menu_cambio_ruolo);
        btnVisualizzaQuiz = findViewById(R.id.menu_visualizza_quiz);

        listaQuizHome = findViewById(R.id.lista_quiz_home);
        messaggioBenvenuto = findViewById(R.id.messaggio_benvenuto);

        // 2. Logica per aprire/chiudere il menu (Toggle)
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Controlliamo lo stato attuale della visibilità
                if (menuTendina.getVisibility() == View.VISIBLE) {
                    // Se è visibile -> Nascondilo
                    menuTendina.setVisibility(View.GONE);
                } else {
                    // Se è nascosto -> Mostralo
                    menuTendina.setVisibility(View.VISIBLE);
                }
            }
        });

        // 3. Gestione click su "Cambio Ruolo"
        btnCambioRuolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Hai cliccato: Cambio Ruolo", Toast.LENGTH_SHORT).show();

                // Qui in futuro metterai l'Intent per cambiare pagina
                // Intent intent = new Intent(HomeActivity.this, AltraActivity.class);
                // startActivity(intent);

                menuTendina.setVisibility(View.GONE);
            }
        });

        // 4. Gestione click su "Visualizza Quiz"
        btnVisualizzaQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chiudi il menu
                menuTendina.setVisibility(View.GONE);

                // Nascondi il messaggio di benvenuto
                messaggioBenvenuto.setVisibility(View.GONE);

                listaQuizHome.setVisibility(View.VISIBLE);

                // lista appare vuota finché non la collego
                // al database con un "Adapter"
            }
        });
    }
}