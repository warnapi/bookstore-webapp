package com.bookstore.dao;

import com.bookstore.model.Review;
import com.bookstore.model.ReviewStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с отзывами.
 */
public class ReviewDao extends BaseDao {
    
    /**
     * Создаёт новый отзыв.
     */
    public Review create(Review review) throws SQLException {
        String sql = """
            INSERT INTO reviews (user_id, book_id, book_title, user_name, rating, text)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, review.userId());
            stmt.setLong(2, review.bookId());
            stmt.setString(3, review.bookTitle());
            stmt.setString(4, review.userName());
            stmt.setInt(5, review.rating());
            stmt.setString(6, review.text());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Не удалось создать отзыв");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    // created_at генерируется базой данных
                    String selectSql = "SELECT created_at FROM reviews WHERE id = ?";
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                        selectStmt.setLong(1, id);
                        try (ResultSet selectRs = selectStmt.executeQuery()) {
                            if (selectRs.next()) {
                                return new Review(
                                    id,
                                    review.userId(),
                                    review.bookId(),
                                    review.bookTitle(),
                                    review.userName(),
                                    review.rating(),
                                    review.text(),
                                    ReviewStatus.PENDING_MODERATION,
                                    selectRs.getTimestamp("created_at").toLocalDateTime()
                                );
                            }
                        }
                    }
                }
            }
        }
        
        throw new SQLException("Не удалось создать отзыв");
    }
    
    /**
     * Находит отзыв по ID.
     */
    public Optional<Review> findById(Long id) throws SQLException {
        String sql = """
            SELECT id, user_id, book_id, book_title, user_name, rating, text, 
                   status, created_at
            FROM reviews WHERE id = ?
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
     * Получает все одобренные отзывы для книги.
     */
    public List<Review> findByBookId(Long bookId) throws SQLException {
        String sql = """
            SELECT id, user_id, book_id, book_title, user_name, rating, text, 
                   status, created_at
            FROM reviews 
            WHERE book_id = ? AND status = 'APPROVED'
            ORDER BY created_at DESC
            """;
        
        return executeQuery(sql, bookId);
    }
    
    /**
     * Получает все отзывы пользователя.
     */
    public List<Review> findByUserId(Long userId) throws SQLException {
        String sql = """
            SELECT id, user_id, book_id, book_title, user_name, rating, text, 
                   status, created_at
            FROM reviews 
            WHERE user_id = ?
            ORDER BY created_at DESC
            """;
        
        return executeQuery(sql, userId);
    }
    
    /**
     * Получает все отзывы с ожиданием модерации.
     */
    public List<Review> findPendingForModeration() throws SQLException {
        String sql = """
            SELECT id, user_id, book_id, book_title, user_name, rating, text, 
                   status, created_at
            FROM reviews 
            WHERE status = 'PENDING_MODERATION'
            ORDER BY created_at ASC
            """;
        
        return executeQuery(sql);
    }
    
    /**
     * Обновляет статус отзыва.
     */
    public boolean updateStatus(Long reviewId, ReviewStatus status) throws SQLException {
        String sql = "UPDATE reviews SET status = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setLong(2, reviewId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Удаляет отзыв.
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Проверяет, оставил ли пользователь отзыв о книге.
     */
    public boolean hasUserReviewedBook(Long userId, Long bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND book_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Получает количество отзывов для книги.
     */
    public long countByBookId(Long bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews WHERE book_id = ? AND status = 'APPROVED'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Получает средний рейтинг книги.
     */
    public double getAverageRating(Long bookId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE book_id = ? AND status = 'APPROVED'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        
        return 0.0;
    }
    
    /**
     * Получает все отзывы с пагинацией (для администратора).
     */
    public List<Review> findAll(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, user_id, book_id, book_title, user_name, rating, text, 
                   status, created_at
            FROM reviews 
            ORDER BY created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        List<Review> reviews = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        }
        
        return reviews;
    }
    
    /**
     * Получает общее количество отзывов.
     */
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews";
        
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
     * Выполняет запрос и возвращает список отзывов.
     */
    private List<Review> executeQuery(String sql, Object... params) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        }
        
        return reviews;
    }
    
    /**
     * Маппинг строки ResultSet в объект Review.
     */
    private Review mapRow(ResultSet rs) throws SQLException {
        return new Review(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("book_id"),
            rs.getString("book_title"),
            rs.getString("user_name"),
            rs.getInt("rating"),
            rs.getString("text"),
            ReviewStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
