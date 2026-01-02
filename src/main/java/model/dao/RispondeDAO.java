package model.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import model.entity.Risponde;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;

import java.util.List;

public class RispondeDAO {

    @Inject
    private EntityManager em;

    public Risponde findById(int id) throws AppException, EntityNotFoundException {
        if (id <= 0) {
            throw new AppException("Id invalido");
        }

        return em.find(Risponde.class, id);
    }

    public List<Risponde> findAll(int pageNumber, int pageSize) throws EntityNotFoundException, AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Risponde.faindAll", Risponde.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Risponde> findAllByUtente(Utente utente, int pageNumber, int pageSize) throws EntityNotFoundException, EmptyFild {
        if (utente == null) {
            throw new EmptyFild("Utente invalido");
        }
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Risponde.faindAllByUtente", Risponde.class)
                .setParameter("utente", utente)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void insert(Risponde ris) throws AppException, EmptyFild {
        if (ris == null) {
            throw new EmptyFild("Risponde invalido");
        }

        em.persist(ris);
    }

    public void update(Risponde ris) throws AppException, EmptyFild {
        if (ris == null) {
            throw new EmptyFild("Risponde invalido");
        }

        em.merge(ris);
    }

    public void delete(Risponde ris) throws AppException, EmptyFild {
        if (ris == null) {
            throw new EmptyFild("Risponde invalido");
        }

        em.remove(em.merge(ris));
    }
}
