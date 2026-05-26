package com.bookstore.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Утилита для управления пулом соединений с базой данных HikariCP.
 * Осуществляет инициализацию и предоставление DataSource.
 */
public class DatabaseUtil {
    
    private static DataSource dataSource;
    private static final Logger logger = Logger.getLogger(DatabaseUtil.class.getName());
    
    /**
     * Инициализирует DataSource из параметров контекста веб-приложения.
     * Вызывается один раз при запуске приложения.
     */
    public static void initDataSource(ServletContext context) {
        logger.info("initDataSource called");
        if (dataSource != null) {
            logger.warning("dataSource already exists, skipping");
            return;
        }
        
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(context.getInitParameter("db.url"));
        config.setUsername(context.getInitParameter("db.username"));
        config.setPassword(context.getInitParameter("db.password"));
        
        int maxPoolSize = Integer.parseInt(
            context.getInitParameter("db.maxPoolSize")
        );
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // Дополнительные настройки для PostgreSQL
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("stringtype", "unspecified");
        config.addDataSourceProperty("currentSchema", "public");
        
        dataSource = new HikariDataSource(config);
        logger.info("HikariDataSource created");
    }
    
    /**
     * Возвращает инициализированный DataSource.
     * 
     * @return DataSource для подключения к базе данных
     * @throws IllegalStateException если DataSource еще не инициализирован
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource не инициализирован");
        }
        return dataSource;
    }
    
    /**
     * Закрывает DataSource и освобождает все соединения.
     * Вызывается при остановке приложения.
     */
    public static void closeDataSource() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.close();
            dataSource = null;
        }
    }
    
    /**
     * Получает соединение из пула.
     * 
     * @return соединение с базой данных
     * @throws SQLException если не удалось получить соединение
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}