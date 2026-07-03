package config;

import jakarta.persistence.*;

public class JpaUtil {

    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("jpqlPU");

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
