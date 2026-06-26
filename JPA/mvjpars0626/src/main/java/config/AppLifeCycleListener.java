package config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppLifeCycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 驗證 EMF 初始化成功
        JpaUtil.createEntityManager().close();
        sce.getServletContext().log("JPA EMF initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.close();
        sce.getServletContext().log("JPA EMF closed.");
    }
}
