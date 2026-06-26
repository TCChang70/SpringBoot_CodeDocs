package repository;

import config.JpaUtil;
import model.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository implements MyRepository<Employee, Integer> {

    @Override
    public Employee save(Employee emp) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(emp);          // INSERT
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
            return em.createQuery(
                    "SELECT e FROM Employee e ORDER BY e.id",
                    Employee.class).getResultList();
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
            Employee merged = em.merge(emp);  // UPDATE
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
            if (emp != null) em.remove(emp);   // DELETE
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ── 進階查詢 ────────────────────────────────────

    public List<Employee> findByDepartment(String dept) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e WHERE LOWER(e.department) = LOWER(:dept) ORDER BY e.name",
                    Employee.class)
                .setParameter("dept", dept)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Employee> findAllPaged(int page, int size) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e ORDER BY e.id",
                    Employee.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
	public boolean existsById(Integer id) {
		EntityManager em = JpaUtil.createEntityManager();
		try {
			return em.find(Employee.class, id) != null;
		} finally {
			em.close();
		}
	}
}
