package model.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import model.entity.Domanda;
import model.entity.Quiz;
import model.exception.AppException;
import model.exception.EmptyFild;

import java.util.List;

public class DomandaDAO {

    @Inject
    private EntityManager em;

    public Domanda findById(int id) throws AppException {
        if (id <= 0) {
            throw new AppException("Id invalido");
        }
        return em.find(Domanda.class, id);
    }

    public List<Domanda> findAll(int pageNumber, int pageSize) throws AppException, EntityNotFoundException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Domanda.findAll", Domanda.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Domanda> findAllByQuiz(int pageNumber, int pageSize, Quiz quiz) throws AppException, EntityNotFoundException, Exception {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }
        if (quiz == null) {
            throw new EmptyFild("Quiz invalido");
        }

        return em.createNamedQuery("Domanda.findAllByQuiz", Domanda.class)
                .setParameter("quiz", quiz)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void delete(Domanda d) throws EmptyFild, EntityNotFoundException {
        if (d == null) {
            throw new EmptyFild("Domanda invalido");
        }
        em.remove(em.merge(d));
    }

    public void insert(Domanda d) throws EmptyFild, EntityNotFoundException {
        if (d == null) {
            throw new EmptyFild("Domanda invalido");
        }
        em.persist(d);
    }

    public void update(Domanda d) throws EmptyFild, EntityNotFoundException {
        if (d == null) {
            throw new EmptyFild("Domanda invalido");
        }

        em.merge(d);
    }
}
