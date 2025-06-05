package org.example.sudoku.web;

import org.example.sudoku.util.FlywayMigrator;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Starting Sudoku web application...");
        
        try {
            // запускаем миграцию базы при старте приложения
            FlywayMigrator.migrate();
            System.out.println("✅ Database migration completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error during database initialization: " + e.getMessage());
            e.printStackTrace();
            // не останавливаем приложение, просто пишем ошибку
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Shutting down Sudoku web application...");
    }
}
