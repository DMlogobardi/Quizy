package model.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import model.entity.Fa;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;

import java.util.List;

public class FaDAO {

    @Inject
    private EntityManager em;

    public Fa faindById(int id) throws AppException, EntityNotFoundException {
        if(id <= 0){
            throw new AppException("id invalido");
        }
        return em.find(Fa.class, id);
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
        if (utente == null) {
            throw new EmptyFild("Utente invalido");
        }

        return em.createNamedQuery("Fa.findAllByUtente", Fa.class)
                .setParameter("utente", utente)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void insert(Fa f) throws EmptyFild {
        if (f == null) {
            throw new EmptyFild("Fa invalida");
        }
        em.persist(f);
    }

    public void update(Fa f) throws EmptyFild {
        if (f == null) {
            throw new EmptyFild("Fa invalida");
        }
        em.merge(f);
    }

    public void delete(Fa f) throws EmptyFild {
        if (f == null) {
            throw new EmptyFild("Fa invalida");
        }
        em.remove(em.merge(f));
    }
}
