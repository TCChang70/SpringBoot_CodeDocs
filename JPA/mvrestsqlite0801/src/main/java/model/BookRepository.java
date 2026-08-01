package model;

import config.JpaUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class BookRepository {

 public List<Book> findAll() {
     EntityManager em = JpaUtil.createEntityManager();
     try {
         return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                  .getResultList();
     } finally {
         em.close();
     }
 }
}
