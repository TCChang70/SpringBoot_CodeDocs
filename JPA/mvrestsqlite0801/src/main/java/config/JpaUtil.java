package config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
 private static final EntityManagerFactory emf;

 static {
     emf = Persistence.createEntityManagerFactory("bookstorePU");
 }

 public static EntityManager createEntityManager() {
     return emf.createEntityManager();
 }

 public static void close() {
     if (emf != null && emf.isOpen()) {
         emf.close();
     }
 }
}
