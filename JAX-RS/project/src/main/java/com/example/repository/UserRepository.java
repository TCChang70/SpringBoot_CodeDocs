package com.example.repository;

import com.example.config.JpaUtil;
import com.example.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByUsername(String username) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getSingleResult();
            return Optional.ofNullable(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public User save(User user) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean existsByUsername(String username) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
