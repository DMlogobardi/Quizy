package model.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import model.entity.Domanda;
import model.entity.Risposta;
import model.exception.AppException;
import model.exception.EmptyFild;

import java.util.List;

public class RispostaDAO {

    @Inject
    private EntityManager em;

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
        return em.find(Risposta.class, id);
    }

    public List<Risposta> findByDomanda(Domanda domanda, int pageNumber, int pageSize) throws EntityNotFoundException, AppException, EmptyFild {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        if (domanda == null) {
            throw new EmptyFild("Domanda non valido");
        }

        Query q = em.createNamedQuery("Risposta.faindAllByDomanda", Risposta.class);

        q.setParameter("domanda", domanda);
        return q.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void insert(Risposta r) throws EntityNotFoundException, EmptyFild {
        if(r == null) {
            throw new EmptyFild("Risposta is empty");
        }

        em.persist(r);
    }

    public void update(Risposta r) throws EntityNotFoundException, EmptyFild {
        if(r == null) {
            throw new EmptyFild("Risposta is empty");
        }
        em.merge(r);
    }

    public void delete(Risposta r) throws EntityNotFoundException, EmptyFild {
        if(r == null) {
            throw new EmptyFild("Risposta is empty");
        }

        em.remove(em.merge(r));
    }
}
