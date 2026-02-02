package model.dao;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;

@Dependent
public class UtenteDAO {

    @Inject
    private EntityManager em;

    public UtenteDAO() {
    }

    public List<Utente> findAllPaginated(int pageNumber, int pageSize) throws AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Utente.findAll", Utente.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public Utente findForLogin(String username) throws UserNotFoundException, EmptyFild {
        if(username == null || username.isEmpty()) {
            throw new EmptyFild("Username non trovato");
        }

        try {
            Query q = em.createNamedQuery("Utente.login", Utente.class);
            q.setParameter("username", username);
            return (Utente) q.getSingleResult();
        } catch (NoResultException e) {
            // Se non c'è risultato, rilancio solo la mia eccezione
            throw new UserNotFoundException("Username non trovato");
        } catch (Exception e) {
            // Se vuoi loggare, fallo ma rilancia solo la tua eccezione
            System.err.println("Errore generico durante login: " + e.getMessage());
            throw new UserNotFoundException("Username non trovato");
        }
    }

    public Utente findById(Integer id) throws UserNotFoundException, AppException {
        if (id <= 0) {
            throw new AppException("Id non valido");
        }

        Query q = em.createNamedQuery("Utente.findById", Utente.class);
        q.setParameter("id", id);

        try {
            return (Utente) q.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserNotFoundException("id non trovato");
        }
    }


    public void register(Utente u) throws RegisterFailed {
        if(u == null) {
            throw new RegisterFailed("dati utente vuoti");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(u);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new RegisterFailed("Errore durante la registrazione");
        }
    }

    public void update(Utente u) throws EmptyFild, AppException {
        if(u == null || u.getId() == null || u.getId() <= 0) {
            throw new EmptyFild("dati utente vuoti");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Utente esistente = em.find(Utente.class, u.getId());
            if (esistente == null) {
                throw new EntityNotFoundException("Impossibile aggiornare: Utente non trovato");
            }

            em.merge(u);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante l'update");
        }
    }

    public void delete(Utente u) throws EmptyFild, UserNotFoundException {
        if(u == null || u.getId() == null || u.getId() <= 0) {
            throw new EmptyFild("dati utente vuoti");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.merge(u));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new UserNotFoundException("utente non trovato");
        }
    }

}
