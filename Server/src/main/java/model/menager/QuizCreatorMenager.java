package model.menager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import model.dao.QuizDAO;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.*;
import model.utility.AccessControlService;
import model.utility.PassCrypt;
import model.utility.QuizLog;
import model.utility.SessionLog;

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
        accessControl.checkCreatore(token);
    }


    public String upUserRole(String token) throws AppException {
        if (!logBeble.isAlive(token)) {
            throw new AppException("Sessione non attiva o token non valido");
        }
        String newToken = "";

        Utente u = logBeble.getUtente(token);
        if (u.getIsCreatore()) {
            newToken = accessControl.newTokenByRole("creatore", u);
            logBeble.rimuovi(token);
            logBeble.aggiungi(newToken, u);
            quizLog.clearQuiz(u);
            return newToken;
        }
        throw new AppException("Unauthorized");
    }

    public void createQuiz(Quiz quiz, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);

            if (quiz.getPasswordQuiz() != null) {
                if(!quiz.getPasswordQuiz().isEmpty()) {
                    String hashedPassword = crypt.hashPassword(quiz.getPasswordQuiz());
                    quiz.setPasswordQuiz(hashedPassword);
                }
            }

            Utente u = logBeble.getUtente(token);
            quiz.setUtente(u);

            if (quiz.getDomande() != null) {
                for (Domanda domanda : quiz.getDomande()) {
                    domanda.setQuiz(quiz);
                    domanda.setId(null);

                    if (domanda.getRisposte() != null) {
                        for (Risposta risposta : domanda.getRisposte()) {
                            risposta.setDomanda(domanda);
                            risposta.setId(null);
                        }
                    }
                }
            }

            dao.insert(quiz);

        }catch (TokenExpiredException e){
            throw new QuizServiceException("token expired, logout forzato");
        } catch (AppException e) {
            e.printStackTrace();
            throw new QuizServiceException("Error creating quiz");
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuizServiceException("Error creating quiz");
        }

    }

    public void deleteQuiz(Quiz quiz, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);

            Utente u = logBeble.getUtente(token);
            quizLog.rimuoviSingoloQuiz(u, quiz.getId());

            dao.delete(quiz.getId(), u);

        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (EntityNotFoundException e) {
            throw new QuizServiceException("Error creating quiz");
        } catch (AppException e) {
            e.printStackTrace();
            throw new QuizServiceException("Error creating quiz");
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuizServiceException("Error creating quiz");
        }
    }

    public void aggiornaQuiz(Quiz quiz, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);

            Utente u = logBeble.getUtente(token);
            Quiz oldQuiz = quizLog.getQuiz(u, quiz.getId());

            dao.update(quiz, oldQuiz, u);
            quizLog.aggiornaSingoloQuiz(u, quiz);

        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (EntityNotFoundException e) {
            throw new QuizServiceException("Error creating quiz");
        } catch (AppException e) {
            e.printStackTrace();
            throw new QuizServiceException("Error updating quiz");
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuizServiceException("Error creating quiz");
        }
    }

    public List<Quiz> getQuizzes(int pageNumber, String token) throws QuizServiceException, InvalidRole {
        try {
            tokenCheck(token);
            if(pageNumber < 0)
                throw new QuizServiceException("pagina invalida");

            Utente u = logBeble.getUtente(token);

            int pageSize = 10;

            List<Quiz> pagedQuizzes = quizLog.getQuizPaginati(u, pageNumber, pageSize);

            if (pagedQuizzes.isEmpty()) {
                pagedQuizzes = dao.findAllByUtente(pageNumber, u);

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
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuizServiceException("Error creating quiz");
        }
    }
}
