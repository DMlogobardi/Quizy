package controller.utility;

import jakarta.inject.Singleton;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.AppException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class QuizLog {

    private final Map<Utente, Map<Integer, Quiz>> logQuizBible = new ConcurrentHashMap<>();

    public QuizLog() {
    }

    public void aggiungi(Utente utente, Quiz quiz) throws AppException {
        Map<Integer, Quiz> userQuizzes = logQuizBible.computeIfAbsent(
                utente,
                u -> new ConcurrentHashMap<>()
        );

        if (userQuizzes.putIfAbsent(quiz.getId(), quiz) != null) {
            throw new AppException("Errore: Il quiz con ID " + quiz.getId() + " è già presente per questo utente.");
        }
    }

    public List<Quiz> getQuiz(Utente utente) throws AppException {
        Map<Integer, Quiz> userQuizzes = logQuizBible.get(utente);

        if (userQuizzes == null || userQuizzes.isEmpty()) {
            throw new AppException("Quiz non presenti");
        }

        return new ArrayList<>(userQuizzes.values());
    }

    public void clearQuiz(Utente utente) throws AppException {
        Map<Integer, Quiz> removed = logQuizBible.remove(utente);

        if (removed == null) {
            throw new AppException("Impossibile cancellare: l'utente " + utente.getNome() + " non ha quiz registrati.");
        }
    }

    public Quiz getQuiz(Utente utente, int id) throws AppException {
        Map<Integer, Quiz> userQuizzes = logQuizBible.get(utente);

        if (userQuizzes == null) {
            throw new AppException("Nessun quiz registrato per questo utente.");
        }

        Quiz quiz = userQuizzes.get(id);
        if (quiz == null) {
            throw new AppException("Quiz con ID " + id + " non trovato.");
        }

        return quiz;
    }

    public List<Quiz> getQuizPaginati(Utente utente, int pageNumber, int pageSize) {
        Map<Integer, Quiz> userQuizzes = logQuizBible.get(utente);
        if (userQuizzes == null || userQuizzes.isEmpty()) {
            return new ArrayList<>();
        }

        // Trasformiamo i valori in lista e li ordiniamo per ID
        // Questo garantisce che la paginazione sia coerente
        int offset = pageNumber * pageSize;

        return userQuizzes.values().stream()
                .sorted((q1, q2) -> Integer.compare(q1.getId(), q2.getId())) // Ordinamento stabile
                .skip(offset)                                              // Salta i risultati precedenti
                .limit(pageSize)                                           // Prende solo la pagina richiesta
                .toList();
    }
}
