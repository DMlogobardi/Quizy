package controller.menager;

import controller.utility.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.dao.QuizDAO;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.*;

import java.util.List;

@ApplicationScoped
public class QuizUserMenager {

    @Inject
    private PassCrypt crypt;

    @Inject
    private SessionLog logBeble;

    @Inject
    private QuizLog quizLog;

    @Inject
    private AccessControlService accessControl;

    @Inject
    private QuizDAO dao;

    public QuizUserMenager() {
    }

    private void tokenCheck(String token) throws TokenExpiredException, InvalidToken, InvalidRole, AppException {
        if (!logBeble.isAlive(token)) {
            throw new AppException("Sessione non attiva o token non valido");
        }
        accessControl.checkCompilatore(token);
    }

    public String downUserRole (String token) throws AppException {
        if (!logBeble.isAlive(token)) {
            throw new AppException("Sessione non attiva o token non valido");
        }
        String newToken = "";

        Utente u = logBeble.getUtente(token);
        if (u.getIsCompilatore()) {
            newToken = accessControl.newTokenByRole("compilatore", u);
            logBeble.rimuovi(token);
            quizLog.clearQuiz(u);
            logBeble.aggiungi(newToken, u);
            return newToken;
        }
        throw new AppException("Unauthorized");
    }

    public List<Quiz> getQuizzes(int pageNumber, int pageSize, String token) throws QuizUseException, InvalidRole {
        try {
            tokenCheck(token);
            Utente u = logBeble.getUtente(token);

            List<Quiz> pagedQuizzes = quizLog.getQuizPaginati(u, pageNumber, pageSize);

            if(pagedQuizzes == null || pagedQuizzes.isEmpty()) {
                pagedQuizzes = dao.findAll(pageNumber, pageSize);

                if(pagedQuizzes != null && !pagedQuizzes.isEmpty()) {
                    for (Quiz quiz : pagedQuizzes) {
                        try {
                            quizLog.aggiungi(u, quiz);
                        } catch (AppException e) {

                        }
                    }
                }
            }

            return pagedQuizzes;

        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (AppException e) {
            throw new QuizServiceException("Error getting quizzes");
        }
    }

    public int completaQuiz(Quiz quiz, String token) throws QuizUseException, InvalidRole {
        try {
            tokenCheck(token);
            Utente u = logBeble.getUtente(token);

            Quiz quizCorretto = quizLog.getQuiz(u, quiz.getId());

            if (quizCorretto == null) {
                quizCorretto = dao.findById(quiz.getId());
                if (quizCorretto == null) {
                    throw new QuizUseException("Quiz non trovato");
                }
            }

            int punteggio = 0, corretto = 0, sbagliato = 0;

            List<Domanda> domande = quizCorretto.getDomande();
            for (Domanda domanda : domande) {

            }

        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (AppException e) {
            throw new QuizServiceException("Error getting quizzes");
        }
    }
}
