package model.dao;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import model.entity.Domanda;
import model.entity.Risposta;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;

@Dependent
public class RispostaDAO {

    @Inject
    private EntityManager em;

    public RispostaDAO() {
    }

    public List<Risposta> findAll(int pageNumber, int pageSize) throws EntityNotFoundException, AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Risposta.faindAll", Risposta.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public Risposta findById(int id) throws EntityNotFoundException {
        if (id <= 0) {
            throw new AppException("Id invalido");
        }

        return em.find(Risposta.class, id);
    }

    public List<Risposta> findByDomanda(Domanda domanda, int pageNumber, int pageSize) throws EntityNotFoundException, AppException, EmptyFild {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        if (domanda == null || domanda.getId() == null || domanda.getId() <= 0) {
            throw new EmptyFild("Domanda non valido");
        }

        Query q = em.createNamedQuery("Risposta.faindAllByDomanda", Risposta.class);

        q.setParameter("domanda", domanda);
        return q.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void insert(Risposta r) throws EmptyFild {
        if(r == null || r.getDomanda() == null || r.getDomanda().getId() == null || r.getDomanda().getId() <= 0) {
            throw new EmptyFild("Risposta is invalid");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(r);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante la registrazione");
        }
    }

    public void update(Risposta r) throws EntityNotFoundException, EmptyFild {
        if(r == null || r.getId() == null || r.getId() <= 0) {
            throw new EmptyFild("Risposta is empty");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(r);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante l'update");
        }
    }

    public void delete(Risposta r) throws EmptyFild {
        if(r == null || r.getId() == null || r.getId() <= 0) {
            throw new EmptyFild("Risposta is empty");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.merge(r));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new UserNotFoundException("utente non trovato");
        }
    }
}
