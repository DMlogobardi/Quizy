package model.dao;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;

@Dependent
public class QuizDAO {

    @Inject
    private EntityManager em;

    public QuizDAO() {
    }

    public Quiz findById(int id) throws EntityNotFoundException, AppException {
        if (id <= 0) {
            throw new AppException("Id invalido");
        }
        return em.find(Quiz.class, id);
    }

    public List<Quiz> findAll(int pageNumber, int pageSize) throws EntityNotFoundException, AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Quiz.findAll", Quiz.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Quiz> findAllByUtente(int pageNumber, int pageSize, Utente utente) throws EntityNotFoundException, EmptyFild {
        if (utente == null) {
            throw new EmptyFild("Utente invalido");
        }
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Quiz.findAllByUtente", Quiz.class)
                .setParameter("utente", utente)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void insert(Quiz quiz) throws EntityNotFoundException, EmptyFild {
        if (quiz == null) {
            throw new EmptyFild("Quiz invalido");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(quiz);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new EntityNotFoundException("Errore durante l'inserimento quiz");
        }
    }

    public void update(Quiz quiz, Utente u) throws EntityNotFoundException, EmptyFild {
        if (u == null || u.getId() == null) throw new EmptyFild("utente non valido");

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Recuperiamo l'oggetto PERSISTENTE (già collegato al DB)
            Quiz q = em.createQuery(
                            "SELECT q FROM Quiz q WHERE q.id = :id AND q.utente.id = :userId", Quiz.class)
                    .setParameter("id", quiz.getId())
                    .setParameter("userId", u.getId())
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("quiz non trovato"));

            // 2. Aggiorniamo i dati semplici di Quiz
            q.setTitolo(quiz.getTitolo());
            q.setDescrizione(quiz.getDescrizione());
            q.setDifficolta(quiz.getDifficolta());
            q.setTempo(quiz.getTempo());
            q.setNumeroDomande(quiz.getNumeroDomande());
            q.setPasswordQuiz(quiz.getPasswordQuiz());

            // 3. Gestione Domande (Sostituzione lista)
            // Fondamentale: pulire e ricollegare per attivare orphanRemoval
            q.getDomande().clear();
            if (quiz.getDomande() != null) {
                for (Domanda d : quiz.getDomande()) {
                    d.setQuiz(q); // Collega al quiz persistente
                    if (d.getRisposte() != null) {
                        for (Risposta r : d.getRisposte()) {
                            r.setDomanda(d);
                        }
                    }
                    q.getDomande().add(d);
                }
            }

            // 4. Merge o Persist (Hibernate riconoscerà le differenze)
            em.merge(q);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public void delete(int quizId, Utente u) throws EntityNotFoundException, EmptyFild {
        if (u == null || u.getId() == null) throw new EmptyFild("utente non valido");

        Quiz q = (Quiz) em.createQuery(
                        "SELECT q FROM Quiz q WHERE q.id = :id AND q.utente.id = :userId", Quiz.class)
                .setParameter("id", quizId)
                .setParameter("userId", u.getId())
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("quiz non trovato o non appartiene all'utente"));

        em.getTransaction().begin();
        em.remove(q);
        em.getTransaction().commit();
    }
}
