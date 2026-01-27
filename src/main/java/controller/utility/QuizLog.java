package controller.utility;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.AppException;
import model.mapper.EntityRefresher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class QuizLog {

    @Inject
    private EntityRefresher refresher;

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

        // MODIFICA: Uso stream per riagganciare ogni quiz
        return userQuizzes.values().stream()
                .map(quiz -> refresher.reattach(quiz))
                .collect(Collectors.toList());
    }

    public void clearQuiz(Utente utente) throws AppException {
        logQuizBible.remove(utente);
    }

    public Quiz getQuiz(Utente utente, int id) throws AppException {
        Map<Integer, Quiz> userQuizzes = logQuizBible.get(utente);

        Quiz quiz = userQuizzes != null ? userQuizzes.get(id) : null;

        // MODIFICA: Restituisco l'oggetto riagganciato (o null)
        return refresher.reattach(quiz);
    }

    public List<Quiz> getQuizPaginati(Utente utente, int pageNumber, int pageSize) {
        Map<Integer, Quiz> userQuizzes = logQuizBible.get(utente);
        if (userQuizzes == null || userQuizzes.isEmpty()) {
            return new ArrayList<>();
        }


        int offset = pageNumber * pageSize;

        // MODIFICA: Aggiunto .map(refresher::reattach) dopo il limit
        return userQuizzes.values().stream()
                .sorted((q1, q2) -> Integer.compare(q1.getId(), q2.getId()))
                .skip(offset)
                .limit(pageSize)
                .map(quiz -> refresher.reattach(quiz)) // <--- QUI
                .toList();
    }
}
