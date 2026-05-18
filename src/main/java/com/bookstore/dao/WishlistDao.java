package com.bookstore.dao;

import com.bookstore.model.Wishlist;
import com.bookstore.model.WishlistItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы со списками желаний.
 */
public class WishlistDao extends BaseDao {
    
    /**
     * Создаёт новый список желаний.
     */
    public Wishlist create(Wishlist wishlist) throws SQLException {
        String sql = """
            INSERT INTO wishlists (user_id, name) VALUES (?, ?)
            RETURNING id
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, wishlist.userId());
            stmt.setString(2, wishlist.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Wishlist(rs.getLong("id"), wishlist.userId(), wishlist.name());
                }
            }
        }
        
        throw new SQLException("Не удалось создать список желаний");
    }
    
    /**
     * Находит список желаний по ID.
     */
    public Optional<Wishlist> findById(Long id) throws SQLException {
        String sql = "SELECT id, user_id, name FROM wishlists WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Wishlist(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("name")
                    ));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Получает все списки желаний пользователя.
     */
    public List<Wishlist> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT id, user_id, name FROM wishlists WHERE user_id = ? ORDER BY name";
        
        List<Wishlist> wishlists = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    wishlists.add(new Wishlist(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("name")
                    ));
                }
            }
        }
        
        return wishlists;
    }
    
    /**
     * Обновляет имя списка желаний.
     */
    public boolean updateName(Long wishlistId, String name) throws SQLException {
        String sql = "UPDATE wishlists SET name = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            stmt.setLong(2, wishlistId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Удаляет список желаний.
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM wishlists WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Добавляет книгу в список желаний.
     */
    public WishlistItem addItem(Long wishlistId, Long bookId) throws SQLException {
        String insertSql = """
            INSERT INTO wishlist_items (wishlist_id, book_id) VALUES (?, ?)
            RETURNING id
            """;
        
        String bookSql = """
            SELECT b.id, b.title, b.author, b.price, b.image_url, b.stock
            FROM books b WHERE b.id = ?
            """;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Проверяем, есть ли уже книга в списке
                if (hasItem(wishlistId, bookId)) {
                    conn.rollback();
                    throw new SQLException("Книга уже в списке желаний");
                }
                
                // Добавляем книгу
                Long itemId;
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setLong(1, wishlistId);
                    stmt.setLong(2, bookId);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemId = rs.getLong("id");
                        } else {
                            conn.rollback();
                            throw new SQLException("Не удалось добавить книгу");
                        }
                    }
                }
                
                // Получаем информацию о книге
                try (PreparedStatement stmt = conn.prepareStatement(bookSql)) {
                    stmt.setLong(1, bookId);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            conn.commit();
                            
                            return new WishlistItem(
                                itemId,
                                wishlistId,
                                bookId,
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getBigDecimal("price"),
                                rs.getString("image_url"),
                                rs.getInt("stock") > 0
                            );
                        }
                    }
                }
                
                conn.rollback();
                throw new SQLException("Книга не найдена");
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Удаляет книгу из списка желаний.
     */
    public boolean removeItem(Long wishlistId, Long bookId) throws SQLException {
        String sql = "DELETE FROM wishlist_items WHERE wishlist_id = ? AND book_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, wishlistId);
            stmt.setLong(2, bookId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Получает все товары в списке желаний.
     */
    public List<WishlistItem> getItems(Long wishlistId) throws SQLException {
        String sql = """
            SELECT wi.id, wi.wishlist_id, wi.book_id, b.title, b.author, 
                   b.price, b.image_url, b.stock
            FROM wishlist_items wi
            JOIN books b ON wi.book_id = b.id
            WHERE wi.wishlist_id = ?
            """;
        
        List<WishlistItem> items = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, wishlistId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new WishlistItem(
                        rs.getLong("id"),
                        rs.getLong("wishlist_id"),
                        rs.getLong("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBigDecimal("price"),
                        rs.getString("image_url"),
                        rs.getInt("stock") > 0
                    ));
                }
            }
        }
        
        return items;
    }
    
    /**
     * Проверяет, есть ли книга в списке желаний.
     */
    public boolean hasItem(Long wishlistId, Long bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM wishlist_items WHERE wishlist_id = ? AND book_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, wishlistId);
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
     * Получает количество товаров в списке желаний.
     */
    public int getItemCount(Long wishlistId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM wishlist_items WHERE wishlist_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, wishlistId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
}
