package com.example.quizzy.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.DTO.rispostaDTO;
import com.example.quizzy.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class RisposteAdapter extends RecyclerView.Adapter<RisposteAdapter.RispostaViewHolder> {

    private final List<rispostaDTO> risposte;

    public RisposteAdapter(List<rispostaDTO> risposte) {
        this.risposte = risposte;
    }

    @NonNull
    @Override
    public RispostaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crea_risposta, parent, false);
        return new RispostaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RispostaViewHolder holder, int position) {
        rispostaDTO risposta = risposte.get(position);

        holder.editTextRisposta.setText(risposta.getAffermazione());
        holder.checkboxRispostaCorretta.setChecked(risposta.getFlagRispostaCorretta());

        holder.editTextRisposta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                risposta.setAffermazione(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.checkboxRispostaCorretta.setOnCheckedChangeListener((buttonView, isChecked) -> {
            risposta.setFlagRispostaCorretta(isChecked);
        });

        holder.buttonRemoveRisposta.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                risposte.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, risposte.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return risposte.size();
    }

    static class RispostaViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText editTextRisposta;
        CheckBox checkboxRispostaCorretta;
        ImageButton buttonRemoveRisposta;


        public RispostaViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextRisposta = itemView.findViewById(R.id.edit_text_risposta);
            checkboxRispostaCorretta = itemView.findViewById(R.id.checkbox_risposta_corretta);
            buttonRemoveRisposta = itemView.findViewById(R.id.button_remove_risposta);
        }
    }
}
