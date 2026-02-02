package model.dao;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;

@Dependent
public class DomandaDAO {

    @Inject
    private EntityManager em;

    public DomandaDAO() {
    }

    public Domanda findById(int id) throws AppException {
        if (id <= 0) {
            throw new AppException("Id invalido");
        }
        return em.find(Domanda.class, id);
    }

    public List<Domanda> findAll(int pageNumber, int pageSize) throws AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Domanda.findAll", Domanda.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Domanda> findAllByQuiz(Quiz quiz) throws AppException, EmptyFild {
        if (quiz == null || quiz.getId() == null || quiz.getId() <= 0) {
            throw new EmptyFild("Quiz invalido");
        }

        return em.createNamedQuery("Domanda.findAllByQuiz", Domanda.class)
                .setParameter("quiz", quiz)
                .getResultList();
    }

    public void delete(Domanda d) throws EmptyFild {
        if (d == null || d.getId() == null || d.getId() <= 0) {
            throw new EmptyFild("Domanda invalido");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.merge(d));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new UserNotFoundException("utente non trovato");
        }
    }

    public void insert(Domanda d) throws EmptyFild {
        if (d == null || d.getQuiz() == null || d.getQuiz().getId() == null || d.getQuiz().getId() <= 0) {
            throw new EmptyFild("Domanda invalido");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(d);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante la registrazione");
        }
    }

    public void update(Domanda d) throws EmptyFild, EntityNotFoundException {
        if (d == null || d.getId() == null || d.getId() <= 0) {
            throw new EmptyFild("Domanda invalido");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Domanda esistente = em.find(Domanda.class, d.getId());
            if (esistente == null) {
                throw new EntityNotFoundException("Impossibile aggiornare: Domanda non trovata");
            }

            em.merge(d);
            tx.commit();
        } catch (EntityNotFoundException e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();

            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante l'update");
        }
    }
}
