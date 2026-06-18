package com.example.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JpaUtil.createEntityManager().close();
        sce.getServletContext().log("JPA EntityManagerFactory initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.close();
        sce.getServletContext().log("JPA EntityManagerFactory closed.");
    }
}
