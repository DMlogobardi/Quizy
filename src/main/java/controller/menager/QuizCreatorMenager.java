package controller.menager;

import controller.utility.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import model.dao.QuizDAO;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.*;

import java.util.List;

@ApplicationScoped
public class QuizCreatorMenager {

    @Inject
    private PassCrypt crypt;

    @Inject
    private SessionLog logBeble;

    @Inject
    private AccessControlService accessControl;

    @Inject
    private QuizDAO dao;

    @Inject
    private QuizLog quizLog;

    public QuizCreatorMenager() {
    }

    private void tokenCheck(String token) throws TokenExpiredException, InvalidToken, InvalidRole, AppException {
        if (!logBeble.isAlive(token)) {
            throw new AppException("Sessione non attiva o token non valido");
        }
        accessControl.checkCompilatore(token);
    }

    public String upUserRole(String token) throws AppException {
        tokenCheck(token);
        String newToken = "";

        Utente u = logBeble.getUtente(token);
        if (u.getIsCreatore()) {
            newToken = accessControl.newTokenByRole("creatore", u);
            logBeble.rimuovi(token);
            logBeble.aggiungi(newToken, u);
            return newToken;
        }
        throw new AppException("Unauthorized");
    }

    public void createQuiz(Quiz quiz, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);

            if (quiz.getPasswordQuiz() != null) {
                String hashedPassword = crypt.hashPassword(quiz.getPasswordQuiz());
                quiz.setPasswordQuiz(hashedPassword);
            }

            Utente u = logBeble.getUtente(token);
            quiz.setUtente(u);

            dao.insert(quiz);

        }catch (TokenExpiredException e){
            throw new QuizServiceException("token expired, logout forzato");
        } catch (AppException e) {

            throw new QuizServiceException("Error creating quiz");
        }

    }

    public void deleteQuiz(Quiz quiz, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);

            Utente u = logBeble.getUtente(token);

            dao.delete(quiz.getId(), u);

        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (EntityNotFoundException e) {
            throw new QuizServiceException("Error creating quiz");
        } catch (AppException e) {
            throw new QuizServiceException("Error creating quiz");
        }
    }

    public void aggiornaQuiz(Quiz quiz, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);

            Utente u = logBeble.getUtente(token);

            dao.update(quiz.getId(), u);
        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (EntityNotFoundException e) {
            throw new QuizServiceException("Error creating quiz");
        } catch (AppException e) {
            throw new QuizServiceException("Error updating quiz");
        }
    }

    public List<Quiz> getQuizzes(int pageNumber, int pageSize, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);
            Utente u = logBeble.getUtente(token);

            List<Quiz> pagedQuizzes = quizLog.getQuizPaginati(u, pageNumber, pageSize);

            if (pagedQuizzes.isEmpty()) {
                pagedQuizzes = dao.findAllByUtente(pageNumber, pageSize, u);

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
}
