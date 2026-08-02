package config;

import jakarta.persistence.*;

public class JpaUtil {
    private static final EntityManagerFactory emf;

    static {                                        // 類別載入時只建立一次
        emf = Persistence.createEntityManagerFactory("bookstorePU");
    }

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();           // 每次請求拿一個新 EntityManager
    }
}
