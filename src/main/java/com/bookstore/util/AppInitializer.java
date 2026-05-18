package com.bookstore.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Слушатель инициализации приложения.
 * Вызывается при старте веб-приложения для инициализации ресурсов.
 */
@WebListener
public class AppInitializer implements ServletContextListener {
    
    private static final Logger logger = LogManager.getLogger(AppInitializer.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("Инициализация приложения Online Bookstore...");
            
            // Инициализация пула соединений с БД
            DatabaseUtil.initDataSource(sce.getServletContext());
            
            logger.info("Приложение успешно инициализировано");
        } catch (Exception e) {
            logger.error("Ошибка инициализации приложения", e);
            throw new RuntimeException("Не удалось инициализировать приложение", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            logger.info("Завершение работы приложения...");
            
            // Закрытие пула соединений
            DatabaseUtil.closeDataSource();
            
            logger.info("Приложение завершено");
        } catch (Exception e) {
            logger.error("Ошибка при завершении работы приложения", e);
        }
    }
}
