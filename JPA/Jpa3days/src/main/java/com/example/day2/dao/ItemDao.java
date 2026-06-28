package com.example.day2.dao;

import com.example.common.JpaUtil;
import com.example.day2.dto.ItemSearchCriteria;
import com.example.day2.entity.Category;
import com.example.day2.entity.Item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {

    public void create(Item item) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Item findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Item.class, id);
        } finally {
            em.close();
        }
    }

    public void update(Item item) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(item);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Item item = em.find(Item.class, id);
            if (item != null) {
                em.remove(item);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Item> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i", Item.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Item> findByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery(
                    "SELECT i FROM Item i WHERE i.name LIKE :name", Item.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Item> findByPriceRange(Double min, Double max) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery(
                    "SELECT i FROM Item i WHERE i.price BETWEEN :min AND :max", Item.class);
            query.setParameter("min", min);
            query.setParameter("max", max);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Item> findByCategoryWithJoinFetch(Long categoryId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery(
                    "SELECT i FROM Item i JOIN FETCH i.category LEFT JOIN FETCH i.tags WHERE i.category.id = :id",
                    Item.class);
            query.setParameter("id", categoryId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Item> findAllWithDetails() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery(
                    "SELECT i FROM Item i JOIN FETCH i.category LEFT JOIN FETCH i.tags",
                    Item.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Item> findByCriteria(ItemSearchCriteria criteria) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Item> cq = cb.createQuery(Item.class);
            Root<Item> root = cq.from(Item.class);
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + criteria.getName() + "%"));
            }
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }
            if (criteria.getCategoryId() != null) {
                Join<Item, Category> categoryJoin = root.join("category");
                predicates.add(cb.equal(categoryJoin.get("id"), criteria.getCategoryId()));
            }
            if (criteria.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), criteria.getActive()));
            }
            if (criteria.getTagNames() != null && !criteria.getTagNames().isEmpty()) {
                Join<Object, Object> tagJoin = root.join("tags");
                predicates.add(tagJoin.get("name").in(criteria.getTagNames()));
            }

            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[0]));
            }

            TypedQuery<Item> query = em.createQuery(cq);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Item> findWithQueryHint() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i", Item.class);
            query.setHint("eclipselink.batch", "i.category");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Long countItems() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(i) FROM Item i", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Item> findTopExpensive(int n) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery(
                    "SELECT i FROM Item i ORDER BY i.price DESC", Item.class);
            query.setMaxResults(n);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public int bulkUpdatePrice(Double percentage, Long categoryId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            jakarta.persistence.Query query = em.createQuery(
                    "UPDATE Item i SET i.price = i.price * :pct WHERE i.category.id = :catId");
            query.setParameter("pct", percentage);
            query.setParameter("catId", categoryId);
            int updated = query.executeUpdate();
            em.getTransaction().commit();
            return updated;
        } finally {
            em.close();
        }
    }
}
