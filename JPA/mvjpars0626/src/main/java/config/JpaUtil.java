package config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.logging.Logger;

public class JpaUtil {

    private static final Logger LOG = Logger.getLogger(JpaUtil.class.getName());
    private static final EntityManagerFactory emf;

    static {
        try {
            // 讀取 META-INF/persistence.xml 中 persistence-unit name="jaxrsPU"
            emf = Persistence.createEntityManagerFactory("jaxrsPU");
            LOG.info("EclipseLink EMF initialized successfully.");
        } catch (Exception e) {
            LOG.severe("EMF init failed: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            LOG.info("EMF closed.");
        }
    }
}
