package repository;

import config.JpaUtil;
import model.Product;
import jakarta.persistence.*;
import java.util.List;

public class ProductRepository {

    public List<Product> findActiveProducts() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category WHERE p.status = :status ORDER BY p.price ASC",
                    Product.class)
                .setParameter("status", "ACTIVE")
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Product> searchByName(String keyword) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category WHERE p.name LIKE :keyword ORDER BY p.name",
                    Product.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Product> findByPriceRange(Double min, Double max) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category WHERE p.price BETWEEN :min AND :max ORDER BY p.price",
                    Product.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Product> findLowStockProducts(int threshold) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category WHERE p.stock < :threshold ORDER BY p.stock ASC",
                    Product.class)
                .setParameter("threshold", threshold)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public Product findById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id",
                    Product.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
