package com.example.day3.dao;

import java.time.LocalDateTime;
import java.util.List;

import com.example.common.JpaUtil;
import com.example.day3.entity.Event;
import com.example.day3.entity.Ticket;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

public class TicketDao {

    public void create(Ticket ticket) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ticket);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Ticket findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Ticket.class, id);
        } finally {
            em.close();
        }
    }

    public List<Ticket> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Ticket> query = em.createQuery("SELECT t FROM Ticket t", Ticket.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Ticket> findByEventId(Long eventId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Ticket> query = em.createQuery(
                    "SELECT t FROM Ticket t WHERE t.event.id = :eventId", Ticket.class);
            query.setParameter("eventId", eventId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Ticket ticket = em.find(Ticket.class, id);
            if (ticket != null) {
                em.remove(ticket);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public String bookTicket(Long eventId, String buyerName, int quantity) {
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            EntityManager em = JpaUtil.getEntityManager();
            try {
                em.getTransaction().begin();
                Event event = em.find(Event.class, eventId);
                if (event == null) {
                    em.getTransaction().rollback();
                    return "{\"error\":\"Event not found\"}";
                }
                if (event.getAvailableTickets() < quantity) {
                    em.getTransaction().rollback();
                    return "{\"error\":\"Insufficient tickets available\"}";
                }
                Ticket ticket = new Ticket();
                ticket.setBuyerName(buyerName);
                ticket.setQuantity(quantity);
                ticket.setStatus("BOOKED");
                ticket.setBookingDate(LocalDateTime.now());
                ticket.setEvent(event);
                event.decrementAvailable(quantity);
                em.persist(ticket);
                em.merge(event);
                em.getTransaction().commit();
                return "{\"success\":true,\"ticketId\":" + ticket.getId()
                        + ",\"buyerName\":\"" + buyerName + "\",\"quantity\":" + quantity + "}";
            } catch (OptimisticLockException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                if (attempt == maxRetries - 1) {
                    return "{\"error\":\"Concurrent booking conflict after " + maxRetries + " attempts\"}";
                }
            } finally {
                if (em.isOpen()) em.close();
            }
        }
        return "{\"error\":\"Booking failed after retries\"}";
    }
}
