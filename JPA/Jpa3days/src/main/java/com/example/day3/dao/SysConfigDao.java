package com.example.day3.dao;

import java.time.LocalDateTime;
import java.util.List;

import com.example.common.JpaUtil;
import com.example.day3.entity.SysConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class SysConfigDao {

    public void create(SysConfig config) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(config);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public SysConfig findByKey(String key) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<SysConfig> query = em.createQuery(
                    "SELECT s FROM SysConfig s WHERE s.configKey = :key", SysConfig.class);
            query.setParameter("key", key);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public SysConfig findByKeyWithCache(String key) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<SysConfig> query = em.createQuery(
                    "SELECT s FROM SysConfig s WHERE s.configKey = :key", SysConfig.class);
            query.setParameter("key", key);
            query.setHint("jakarta.persistence.cache.retrieveMode", "USE");
            query.setHint("jakarta.persistence.cache.storeMode", "USE");
            SysConfig result = query.getSingleResult();
            return result;
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<SysConfig> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<SysConfig> query = em.createQuery("SELECT s FROM SysConfig s", SysConfig.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void updateValue(Long id, String newValue) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            SysConfig config = em.find(SysConfig.class, id);
            if (config != null) {
                config.setConfigValue(newValue);
                config.setUpdatedAt(LocalDateTime.now());
                em.merge(config);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            SysConfig config = em.find(SysConfig.class, id);
            if (config != null) {
                em.remove(config);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
