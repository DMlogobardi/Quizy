package model.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import model.entity.Ticket;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.EmptyFild;

import java.util.List;

public class TicketDAO {

    @Inject
    private EntityManager em;

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

        em.remove(em.merge(ticket));
    }

    public void update(Ticket ticket) throws EmptyFild, EntityNotFoundException {
        if(ticket == null) {
            throw new EmptyFild("dati ticket vuoto");
        }

        em.merge(ticket);
    }

    public void insert(Ticket ticket) throws EmptyFild, EntityNotFoundException {
        if(ticket == null) {
            throw new EmptyFild("dati ticket vuoto");
        }

        em.persist(ticket);
    }
}
