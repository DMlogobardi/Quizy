package model;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Application started...");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application stopped...");
        JPAUtil.shutdown();
    }
}
