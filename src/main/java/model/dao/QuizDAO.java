package model.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;

import java.util.List;

public class QuizDAO {

    @Inject
    private EntityManager em;

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

        em.persist(quiz);
    }

    public void update(Quiz quiz) throws EntityNotFoundException, EmptyFild {
        if (quiz == null) {
            throw new EmptyFild("Quiz invalido");
        }

        em.merge(quiz);
    }

    public void delete(Quiz quiz) throws EntityNotFoundException, EmptyFild {
        if (quiz == null) {
            throw new EmptyFild("Quiz invalido");
        }

        em.remove(em.merge(quiz));
    }
}
