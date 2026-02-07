package model.dao;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import model.entity.Fa;
import model.entity.Quiz;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;

@Dependent
public class FaDAO {

    @Inject
    private EntityManager em;

    public FaDAO() {
    }

    public Fa faindById(int id) throws AppException, EntityNotFoundException {
        if(id <= 0){
            throw new AppException("id invalido");
        }
        return em.find(Fa.class, id);
    }

    public Fa findByUtenteQuiz(Quiz quiz, Utente u) throws EntityNotFoundException {
        if(quiz == null || quiz.getId() == null || quiz.getId() <= 0)
            throw new  AppException("quiz non valido");

        if(u == null || u.getId() == null || u.getId() <= 0)
            throw new  AppException("utente non valido");

        return em.createNamedQuery("Fa.findByUserAndQuiz", Fa.class)
                .setParameter("utente", u)
                .setParameter("quiz", quiz)
                .getSingleResult();
    }

    public List<Fa> findAll(int pageNumber, int pageSize) throws AppException, EntityNotFoundException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Fa.findAll", Fa.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Fa> findAllByUtente(int pageNumber, int pageSize, Utente utente) throws AppException, EntityNotFoundException, EmptyFild {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }
        if (utente == null || utente.getId() == null || utente.getId() <= 0) {
            throw new EmptyFild("Utente invalido");
        }

        return em.createNamedQuery("Fa.findAllByUtente", Fa.class)
                .setParameter("utente", utente)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void insert(Fa f) throws EmptyFild {
        if (f == null || f.getUtente()== null || f.getUtente().getId()==null || f.getUtente().getId()<= 0) {
            throw new EmptyFild("Fa invalida");
        }
        if (f.getQuiz() == null || f.getQuiz().getId() == null || f.getQuiz().getId() <= 0) {
            throw new EmptyFild("Fa invalida");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(f);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new RegisterFailed("Errore durante la registrazione");
        }
    }

    public void update(Fa f) throws EmptyFild {
        if (f == null || f.getId() == null || f.getId() <= 0) {
            throw new EmptyFild("Fa invalida");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(f);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante l'update");
        }
    }

    public void delete(Fa f) throws EmptyFild {
        if (f == null || f.getId() == null || f.getId() <= 0) {
            throw new EmptyFild("Fa invalida");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.merge(f));
            tx.commit();
        } catch (EntityNotFoundException e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new UserNotFoundException("utente non trovato");
        }
    }
}
