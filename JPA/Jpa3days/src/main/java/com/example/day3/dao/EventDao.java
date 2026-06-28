package com.example.day3.dao;

import java.util.List;

import com.example.common.JpaUtil;
import com.example.day3.entity.Event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class EventDao {

    public void create(Event event) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(event);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Event findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Event.class, id);
        } finally {
            em.close();
        }
    }

    public List<Event> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e", Event.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Event event) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(event);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Event event = em.find(Event.class, id);
            if (event != null) {
                em.remove(event);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
