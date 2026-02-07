package com.example.quizzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzy.Activity.EditQuizActivity;
import com.example.quizzy.Activity.QuizGameActivity;
import com.example.quizzy.DTO.ListQuizDTO;
import com.example.quizzy.R;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<ListQuizDTO> quizList;
    private String token;
    private QuizActionListener listener;

    private boolean isCreatorMode;


    public QuizAdapter(List<ListQuizDTO> quizList, String token, QuizActionListener listener, boolean isCreatorMode) {
        this.quizList = quizList;
        this.token = token;
        this.listener = listener;
        this.isCreatorMode = isCreatorMode;

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

        if (isCreatorMode) {
            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, EditQuizActivity.class);
                intent.putExtra("QUIZ_ID", quiz.getId());
                intent.putExtra("token", token);
                // Passiamo i dati che ListQuizDTO possiede già
                intent.putExtra("TITOLO", quiz.getTitolo());
                intent.putExtra("DESCRIZIONE", quiz.getDescrizione());
                intent.putExtra("DIFFICOLTA", quiz.getDifficolta());
                intent.putExtra("TEMPO", quiz.getTempo());
                context.startActivity(intent);
            });

        } else {
            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();

                // CONTROLLO PASSWORD
                if (quiz.isPasswordRichiesta()) {
                    mostraDialogPassword(context, quiz);
                } else {
                    // Avvio normale senza password
                    avviaGioco(context, quiz.getId(), null);
                }
            });
        }
        
        // Il cestino per l'eliminazione è sempre visibile in entrambe le modalità
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                // Passiamo l'ID del quiz e la posizione nella lista
                listener.onDeleteClick(quiz.getId(), holder.getAdapterPosition());
            }
        });
    }


    private void mostraDialogPassword(Context context, ListQuizDTO quiz) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Quiz Protetto");
        builder.setMessage("Inserisci la password per iniziare:");

        // Creiamo un EditText dinamicamente
        final android.widget.EditText input = new android.widget.EditText(context);

        input.setId(R.id.input_password_per_quiz);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);        builder.setView(input);

        builder.setPositiveButton("Entra", (dialog, which) -> {
            String passwordInserita = input.getText().toString();
            avviaGioco(context, quiz.getId(), passwordInserita);
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void avviaGioco(Context context, int quizId, String password) {
        Intent intent = new Intent(context, QuizGameActivity.class);
        intent.putExtra("QUIZ_ID", quizId);
        intent.putExtra("token", token);
        if (password != null) {
            intent.putExtra("QUIZ_PASSWORD", password);
        }
        context.startActivity(intent);
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
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
