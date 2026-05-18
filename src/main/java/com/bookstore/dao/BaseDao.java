package com.bookstore.dao;

import com.bookstore.util.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Базовый класс для всех DAO-классов.
 * Предоставляет общие методы для работы с базой данных.
 */
public abstract class BaseDao {
    
    protected static final Logger logger = LogManager.getLogger(BaseDao.class);
    
    /**
     * Получает соединение из пула.
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }
    
    /**
     * Выполняет SQL-запрос с возвратом ключа.
     */
    protected <T> T executeWithKey(Connection conn, String sql, 
                                    StatementHandler<T> handler) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            return handler.handle(stmt.getGeneratedKeys());
        }
    }
    
    /**
     * Интерфейс для обработки результатов выполнения SQL-команд.
     */
    @FunctionalInterface
    protected interface StatementHandler<T> {
        T handle(java.sql.ResultSet resultSet) throws SQLException;
    }
}
