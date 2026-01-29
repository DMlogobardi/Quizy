package com.example.quizzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.Activity.QuizGameActivity;
import com.example.quizzy.DTO.ListQuizDTO;
import com.example.quizzy.R;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<ListQuizDTO> quizList;
    private String token;
    private QuizActionListener listener;


    public QuizAdapter(List<ListQuizDTO> quizList, String token, QuizActionListener listener) {
        this.quizList = quizList;
        this.token = token;
        this.listener = listener;

    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_row, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        ListQuizDTO quiz = quizList.get(position);
        holder.nomeQuiz.setText(quiz.getTitolo());

        // gestione del click
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, QuizGameActivity.class);
            intent.putExtra("QUIZ_ID", quiz.getId()); // Passiamo l'ID del quiz selezionato
            intent.putExtra("token", token);          // Passiamo il token utente
            // Avviamo l'activity
            context.startActivity(intent);
        });

        // cestino
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                // Passiamo l'ID del quiz e la posizione nella lista
                listener.onDeleteClick(quiz.getId(), holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }


    // Metodo per rimuovere visivamente l'elemento dopo che il server ha detto OK
    public void removeItem(int position) {
        if (position >= 0 && position < quizList.size()) {
            quizList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, quizList.size());
        }
    }

    // Metodi per aggiornare la lista (nessuna modifica qui)
    public void updateData(List<ListQuizDTO> newQuizList) {
        this.quizList.clear();
        this.quizList.addAll(newQuizList);
        notifyDataSetChanged();
    }

    public void addData(List<ListQuizDTO> moreQuizList) {
        int currentSize = this.quizList.size();
        this.quizList.addAll(moreQuizList);
        notifyItemRangeInserted(currentSize, moreQuizList.size());
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView nomeQuiz;
        ImageView btnDelete;


        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeQuiz = itemView.findViewById(R.id.nome_quiz_row);
            btnDelete = itemView.findViewById(R.id.btn_delete); // <--- NUOVO
        }
    }
}