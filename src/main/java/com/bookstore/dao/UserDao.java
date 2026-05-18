package com.bookstore.dao;

import com.bookstore.model.Role;
import com.bookstore.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с пользователями.
 * Обеспечивает CRUD операции и поиск пользователей.
 */
public class UserDao extends BaseDao {
    
    /**
     * Создаёт нового пользователя в базе данных.
     * 
     * @param user пользователь для создания
     * @return созданный пользователь с установленным ID
     * @throws SQLException если произошла ошибка базы данных
     */
    public User create(User user) throws SQLException {
        String sql = """
            INSERT INTO users (email, password_hash, role, name) 
            VALUES (?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.email());
            stmt.setString(2, user.passwordHash());
            stmt.setString(3, user.role().name());
            stmt.setString(4, user.name());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Не удалось создать пользователя");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long userId = rs.getLong(1);
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    
                    return new User(
                        userId,
                        user.email(),
                        user.passwordHash(),
                        user.role(),
                        user.name(),
                        createdAt.toLocalDateTime()
                    );
                }
            }
        }
        
        throw new SQLException("Не удалось создать пользователя");
    }
    
    /**
     * Находит пользователя по ID.
     * 
     * @param id идентификатор пользователя
     * @return Optional с пользователем, если найден
     * @throws SQLException если произошла ошибка базы данных
     */
    public Optional<User> findById(Long id) throws SQLException {
        String sql = """
            SELECT id, email, password_hash, role, name, created_at 
            FROM users WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Находит пользователя по email.
     * 
     * @param email адрес электронной почты
     * @return Optional с пользователем, если найден
     * @throws SQLException если произошла ошибка базы данных
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
            SELECT id, email, password_hash, role, name, created_at 
            FROM users WHERE email = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Обновляет данные пользователя.
     * 
     * @param user пользователь для обновления
     * @return true если пользователь был обновлён
     * @throws SQLException если произошла ошибка базы данных
     */
    public boolean update(User user) throws SQLException {
        String sql = """
            UPDATE users SET email = ?, name = ?, role = ? 
            WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.email());
            stmt.setString(2, user.name());
            stmt.setString(3, user.role().name());
            stmt.setLong(4, user.id());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Обновляет пароль пользователя.
     * 
     * @param userId идентификатор пользователя
     * @param newPasswordHash хеш нового пароля
     * @return true если пароль был обновлён
     * @throws SQLException если произошла ошибка базы данных
     */
    public boolean updatePassword(Long userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Удаляет пользователя из базы данных.
     * 
     * @param id идентификатор пользователя
     * @return true если пользователь был удалён
     * @throws SQLException если произошла ошибка базы данных
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Получает всех пользователей с пагинацией.
     * 
     * @param offset смещение для пагинации
     * @param limit максимальное количество записей
     * @return список пользователей
     * @throws SQLException если произошла ошибка базы данных
     */
    public List<User> findAll(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, email, password_hash, role, name, created_at 
            FROM users ORDER BY created_at DESC LIMIT ? OFFSET ?
            """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        
        return users;
    }
    
    /**
     * Получает общее количество пользователей.
     * 
     * @return количество пользователей
     * @throws SQLException если произошла ошибка базы данных
     */
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Проверяет, существует ли пользователь с данным email.
     * 
     * @param email адрес электронной почты
     * @return true если пользователь существует
     * @throws SQLException если произошла ошибка базы данных
     */
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Маппинг строки ResultSet в объект User.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("password_hash"),
            Role.valueOf(rs.getString("role")),
            rs.getString("name"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
