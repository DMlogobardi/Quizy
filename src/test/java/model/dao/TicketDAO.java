package model.dao;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import model.entity.Ticket;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;
import model.exception.RegisterFailed;
import model.exception.UserNotFoundException;

import java.util.List;

@Dependent
public class TicketDAO {

    @Inject
    private EntityManager em;

    public TicketDAO() {
    }

    public List<Ticket> findAll(int pageNumber, int pageSize) throws EntityNotFoundException, AppException {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        return em.createNamedQuery("Ticket.findAll", Ticket.class)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public Ticket findById(int id) throws EntityNotFoundException {
        return em.find(Ticket.class, id);
    }

    public List<Ticket> findByUtente(Utente utente, int pageNumber, int pageSize) throws EntityNotFoundException, AppException, EmptyFild {
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new AppException("Pagina invalida");
        }

        if(utente==null){
            throw new EmptyFild("Utente non valido");
        }

        Query q = em.createNamedQuery("Ticket.faindAllByUtente", Ticket.class);

        q.setParameter("utente", utente);
        q.setFirstResult((pageNumber - 1) * pageSize);
        q.setMaxResults(pageSize);
        q.getResultList();

        return q.getResultList();
    }

    public void delete(Ticket ticket) throws EntityNotFoundException, EmptyFild {
        if(ticket == null) {
            throw new EmptyFild("dati ticket vuoto");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.merge(ticket));
            tx.commit();
        } catch (EntityNotFoundException e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new UserNotFoundException("utente non trovato");
        }
    }

    public void update(Ticket ticket) throws EmptyFild, EntityNotFoundException {
        if(ticket == null) {
            throw new EmptyFild("dati ticket vuoto");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(ticket);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new AppException("Errore durante l'update");
        }
    }

    public void insert(Ticket ticket) throws EmptyFild, EntityNotFoundException {
        if(ticket == null) {
            throw new EmptyFild("dati ticket vuoto");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(ticket);
            tx.commit();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new RegisterFailed("Errore durante la registrazione");
        }
    }
}
