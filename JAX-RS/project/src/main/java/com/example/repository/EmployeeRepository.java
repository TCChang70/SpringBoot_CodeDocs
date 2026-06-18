package com.example.repository;

import com.example.config.JpaUtil;
import com.example.dto.DeptSalaryStats;
import com.example.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository implements Repository<Employee, Integer> {

    @Override
    public Employee save(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(emp);
            tx.commit();
            return emp;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Employee> findById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Employee.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Employee> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Employee> findByDepartment(String department) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept) ORDER BY e.name",
                    Employee.class)
                .setParameter("dept", department)
                .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Employee update(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee merged = em.merge(emp);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, id);
            if (emp != null) em.remove(emp);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Integer id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(e) FROM Employee e WHERE e.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(e) FROM Employee e", Long.class)
                     .getSingleResult();
        } finally {
            em.close();
        }
    }

    public boolean existsByEmail(String email) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(e) FROM Employee e WHERE LOWER(e.email) = LOWER(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public List<Employee> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class)
                     .setFirstResult((page - 1) * size)
                     .setMaxResults(size)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Employee> findBySalaryRange(Double min, Double max, String sortOrder) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            String order = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
            String jpql = "SELECT e FROM Employee e WHERE 1=1";
            if (min != null) jpql += " AND e.salary >= :min";
            if (max != null) jpql += " AND e.salary <= :max";
            jpql += " ORDER BY e.salary " + order;

            var query = em.createQuery(jpql, Employee.class);
            if (min != null) query.setParameter("min", min);
            if (max != null) query.setParameter("max", max);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<DeptSalaryStats> getDepartmentSalaryStats() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT new com.example.dto.DeptSalaryStats(" +
                    "  e.department, COUNT(e), AVG(e.salary), MAX(e.salary)" +
                    ") FROM Employee e GROUP BY e.department ORDER BY AVG(e.salary) DESC",
                    DeptSalaryStats.class)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
