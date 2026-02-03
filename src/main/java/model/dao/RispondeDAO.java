package model.dao;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import model.entity.Domanda;
import model.entity.Risponde;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;

@Dependent
public class RispondeDAO {

    @Inject
    private EntityManager em;

    public RispondeDAO() {
    }

    public Risponde findById(int id) throws AppException {
        if (id <= 0) {
            throw new AppException("Id invalido");
        }

        return em.find(Risponde.class, id);
    }

    public List<Risponde> findAll(int pageNumber, int pageSize) throws AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Risponde.faindAll", Risponde.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Risponde> findAllByUtente(Utente utente, int pageNumber, int pageSize) throws EntityNotFoundException, EmptyFild {
        if (utente == null || utente.getId() == null || utente.getId() <= 0) {
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
        if (ris == null|| ris.getUtente() == null || ris.getUtente().getId() == null || ris.getUtente().getId() <= 0) {
            throw new EmptyFild("Risponde invalido");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(ris);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante la registrazione");
        }
    }

    public void insertAll(List<Risponde> listaRisposte) throws AppException, EmptyFild {
        if (listaRisposte == null || listaRisposte.isEmpty()) {
            throw new EmptyFild("La lista delle risposte è vuota o nulla");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            for (Risponde ris : listaRisposte) {

                if (ris == null|| ris.getUtente() == null || ris.getUtente().getId() == null || ris.getUtente().getId() <= 0) {
                    throw new EmptyFild("Risponde invalido");
                }

                em.persist(ris);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            // Nota: Assicurati che RegisterFailed sia una sottoclasse di AppException
            throw new AppException("Errore durante l'inserimento della lista");
        }
    }

    public void update(Risponde ris) throws AppException, EmptyFild {
        if (ris == null || ris.getId() == null || ris.getId() <= 0) {
            throw new EmptyFild("Risponde invalido");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Risponde esistente = em.find(Risponde.class, ris.getId());
            if (esistente == null) {
                throw new EntityNotFoundException("Impossibile aggiornare: Risponde non trovata");
            }

            em.merge(ris);
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

    public void delete(Risponde ris) throws AppException, EmptyFild {
        if (ris == null || ris.getId() == null || ris.getId() <= 0) {
            throw new EmptyFild("Risponde invalido");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.merge(ris));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new UserNotFoundException("utente non trovato");
        }
    }
}
