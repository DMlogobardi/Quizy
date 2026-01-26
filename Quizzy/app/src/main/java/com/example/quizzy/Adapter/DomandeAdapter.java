package com.example.quizzy.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.DTO.domandaDTO;
import com.example.quizzy.DTO.rispostaDTO;
import com.example.quizzy.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DomandeAdapter extends RecyclerView.Adapter<DomandeAdapter.DomandaViewHolder> {

    private final List<domandaDTO> domande;

    public DomandeAdapter(List<domandaDTO> domande) {
        this.domande = domande;
    }

    @NonNull
    @Override
    public DomandaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crea_domanda, parent, false);
        return new DomandaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DomandaViewHolder holder, int position) {
        domandaDTO domanda = domande.get(position);

        holder.editTextQuesito.setText(domanda.getQuesito());
        holder.editTextPuntiCorretta.setText(String.valueOf(domanda.getPuntiRispostaCorretta()));
        holder.editTextPuntiSbagliata.setText(String.valueOf(domanda.getPuntiRispostaSbagliata()));

        holder.editTextQuesito.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                domanda.setQuesito(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.editTextPuntiCorretta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    domanda.setPuntiRispostaCorretta(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    domanda.setPuntiRispostaCorretta(0);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.editTextPuntiSbagliata.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    domanda.setPuntiRispostaSbagliata(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    domanda.setPuntiRispostaSbagliata(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        RisposteAdapter risposteAdapter = new RisposteAdapter(domanda.getRisposte());
        holder.recyclerViewRisposte.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewRisposte.setAdapter(risposteAdapter);

        holder.buttonAggiungiRisposta.setOnClickListener(v -> {
            domanda.getRisposte().add(new rispostaDTO("", false));
            risposteAdapter.notifyItemInserted(domanda.getRisposte().size() - 1);
        });
    }

    @Override
    public int getItemCount() {
        return domande.size();
    }

    static class DomandaViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText editTextQuesito;
        TextInputEditText editTextPuntiCorretta;
        TextInputEditText editTextPuntiSbagliata;
        RecyclerView recyclerViewRisposte;
        Button buttonAggiungiRisposta;

        public DomandaViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextQuesito = itemView.findViewById(R.id.edit_text_quesito);
            editTextPuntiCorretta = itemView.findViewById(R.id.edit_text_punti_corretta);
            editTextPuntiSbagliata = itemView.findViewById(R.id.edit_text_punti_sbagliata);
            recyclerViewRisposte = itemView.findViewById(R.id.recycler_view_risposte);
            buttonAggiungiRisposta = itemView.findViewById(R.id.button_aggiungi_risposta);
        }
    }
}
