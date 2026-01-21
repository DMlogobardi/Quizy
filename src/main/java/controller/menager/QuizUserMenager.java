package controller.menager;

import controller.utility.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import model.dao.FaDAO;
import model.dao.QuizDAO;
import model.dao.RispondeDAO;
import model.entity.*;
import model.exception.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Inject
    private FaDAO daoFa;

    @Inject
    private RispondeDAO daoRisponde;

    public QuizUserMenager() {
    }

    private void tokenCheck(String token) throws TokenExpiredException, InvalidToken, InvalidRole, AppException {
        if (!logBeble.isAlive(token)) {
            throw new AppException("Sessione non attiva o token non valido");
        }
        accessControl.checkCompilatore(token);
    }

    private int punteggioRisposta(List<Domanda> domande, List<Risposta> risposte) throws AppException {
        Map<Integer, Domanda> domandeById = domande.stream()
                .collect(Collectors.toMap(
                        Domanda::getId,
                        d -> d
                ));

        int punteggio = 0;
        Set<Integer> domandeRisposte = new HashSet<>();

        for (Risposta risposta : risposte) {

            if (risposta.getDomanda() == null || risposta.getDomanda().getId() == null)
                throw new AppException("Risposta non valida");

            Integer domandaId = risposta.getDomanda().getId();

            Domanda d = domandeById.get(domandaId);
            if(d == null)
                throw new AppException("Domanda non valida");

            if(!domandeRisposte.add(domandaId))
                throw new AppException("Domanda risposta piu volte");

            if(risposta.getFlagRispostaCorretta()) {
                punteggio += d.getPuntiRispostaCorretta();
            } else {
                punteggio += d.getPuntiRispostaSbagliata();
            }
        }
        return punteggio;
    }

    private Boolean isComplete(Utente u, Quiz Quiz) {
        try {
            daoFa.findByUtenteQuiz(Quiz, u);
            return true;
        } catch (NoResultException e) {
            return false;
        } catch (EntityNotFoundException e) {
            return false;
        }
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

    public List<Domanda> startQuiz(Quiz quiz, String password, String token) throws QuizUseException, InvalidRole  {
        try {
            tokenCheck(token);
            Utente u = logBeble.getUtente(token);
            Quiz q = quizLog.getQuiz(u, quiz.getId());

            if( q == null) {
                q = dao.findById(quiz.getId());
            }

            if(!crypt.verificaPassword(password, q.getPasswordQuiz())) {
                throw new QuizUseException("impossibile accedere al quiz");
            }

            if(!isComplete(u, q))
                return q.getDomande();
            else
                return null;
        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (AppException e) {
            throw new QuizServiceException("Error getting quizzes");
        }
    }

    public List<Domanda> startQuiz(Quiz quiz, String token) throws QuizUseException, InvalidRole  {
        try {
            tokenCheck(token);
            Utente u = logBeble.getUtente(token);
            Quiz q = quizLog.getQuiz(u, quiz.getId());

            if( q == null) {
                q = dao.findById(quiz.getId());
            }

            if( q.getPasswordQuiz() != null) {
                throw new QuizUseException("password is allowed");
            }

            if(!isComplete(u, q))
                return q.getDomande();
            else
                return null;
        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (AppException e) {
            throw new QuizServiceException("Error getting quizzes");
        }
    }

    public int completaQuiz(Quiz quiz, List<Risposta> risposteClient, String token) throws QuizUseException, InvalidRole {
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

            List<Domanda> domandeDb = quizCorretto.getDomande();

            if (domandeDb == null || risposteClient == null)
                throw new AppException("liste mancanti");

            int punteggio = punteggioRisposta(domandeDb, risposteClient);
            Fa fa = new Fa(u, quizCorretto, punteggio);
            List<Risponde> risposte = new ArrayList<>();
            for (Risposta r : risposteClient) {
                risposte.add(new Risponde(r, u, quizCorretto.getTitolo(), LocalDateTime.now()));
            }
            daoFa.insert(fa);
            daoRisponde.insertAll(risposte);

            return punteggio;

        } catch (TokenExpiredException e) {
            throw new QuizServiceException("token expired, logout forzato");
        } catch (AppException e) {
            throw new QuizServiceException("Error getting quizzes");
        }
    }
}
