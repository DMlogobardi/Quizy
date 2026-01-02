package model.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UtenteDAO {

    @Inject
    private EntityManager em;

    public List<Utente> findAllPaginated(int pageNumber, int pageSize) throws UserNotFoundException, AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Utente.findAll", Utente.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public Utente findForLogin(String username) throws UserNotFoundException {
        Query q = em.createNamedQuery("Utente.login", Utente.class);
        q.setParameter("username", username);

        return (Utente) q.getSingleResult();
    }

    public Utente findById(Integer id) throws UserNotFoundException, AppException {
        if (id <= 0) {
            throw new AppException("Id non valido");
        }

        Query q = em.createNamedQuery("Utente.findById", Utente.class);
        q.setParameter("id", id);

        return (Utente) q.getSingleResult();
    }

    public void register(Utente u) throws RegisterFailed {
        if(u == null) {
            throw new RegisterFailed("dati utente vuoti");
        }

        em.persist(u);
    }

    public void update(Utente u) throws EmptyFild {
        if(u == null) {
            throw new EmptyFild("dati utente vuoti");
        }

        em.merge(u);
    }

    public void delete(Utente u) throws EmptyFild, UserNotFoundException {
        if(u == null) {
            throw new EmptyFild("utente e null");
        }

        try {
            em.remove(em.merge(u));
        } catch (EntityNotFoundException e) {
            Logger.getLogger(UtenteDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new UserNotFoundException("utente non trovato");
        }
    }

}
