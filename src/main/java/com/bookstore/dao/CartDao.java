package com.bookstore.dao;

import com.bookstore.model.CartItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для работы с корзиной покупок.
 */
public class CartDao extends BaseDao {
    
    /**
     * Добавляет товар в корзину.
     */
    public CartItem addToCart(Long userId, Long bookId, int quantity) throws SQLException {
        String checkSql = "SELECT id, quantity FROM cart WHERE user_id = ? AND book_id = ?";
        String insertSql = """
            INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, ?)
            """;
        String updateSql = """
            UPDATE cart SET quantity = quantity + ? WHERE user_id = ? AND book_id = ?
            """;
        String getBookSql = """
            SELECT id, title, author, price, image_url, stock 
            FROM books WHERE id = ?
            """;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Проверяем, есть ли уже товар в корзине
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setLong(1, userId);
                    checkStmt.setLong(2, bookId);
                    
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            // Товар уже есть, увеличиваем количество
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setInt(1, quantity);
                                updateStmt.setLong(2, userId);
                                updateStmt.setLong(3, bookId);
                                updateStmt.executeUpdate();
                            }
                        } else {
                            // Добавляем новый товар
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                insertStmt.setLong(1, userId);
                                insertStmt.setLong(2, bookId);
                                insertStmt.setInt(3, quantity);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                }
                
                // Получаем информацию о книге
                try (PreparedStatement bookStmt = conn.prepareStatement(getBookSql)) {
                    bookStmt.setLong(1, bookId);
                    
                    try (ResultSet rs = bookStmt.executeQuery()) {
                        if (rs.next()) {
                            String bookTitle = rs.getString("title");
                            String bookAuthor = rs.getString("author");
                            BigDecimal bookPrice = rs.getBigDecimal("price");
                            String bookImageUrl = rs.getString("image_url");
                            int stockAvailable = rs.getInt("stock");
                            
                            // Получаем актуальное количество
                            int finalQuantity;
                            try (PreparedStatement quantityStmt = conn.prepareStatement(checkSql)) {
                                quantityStmt.setLong(1, userId);
                                quantityStmt.setLong(2, bookId);
                                try (ResultSet qtyRs = quantityStmt.executeQuery()) {
                                    qtyRs.next();
                                    finalQuantity = qtyRs.getInt("quantity");
                                }
                            }
                            
                            conn.commit();
                            
                            return new CartItem(
                                null, userId, bookId, bookTitle, bookAuthor,
                                bookPrice, bookImageUrl, finalQuantity,
                                stockAvailable, finalQuantity <= stockAvailable
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
     * Получает все товары в корзине пользователя.
     */
    public List<CartItem> getCartItems(Long userId) throws SQLException {
        String sql = """
            SELECT c.id, c.user_id, c.book_id, b.title, b.author, b.price, 
                   b.image_url, b.stock, c.quantity
            FROM cart c
            JOIN books b ON c.book_id = b.id
            WHERE c.user_id = ?
            """;
        
        List<CartItem> items = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    int stockAvailable = rs.getInt("stock");
                    
                    items.add(new CartItem(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getLong("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBigDecimal("price"),
                        rs.getString("image_url"),
                        quantity,
                        stockAvailable,
                        quantity <= stockAvailable
                    ));
                }
            }
        }
        
        return items;
    }
    
    /**
     * Обновляет количество товара в корзине.
     */
    public boolean updateQuantity(Long userId, Long bookId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return removeFromCart(userId, bookId);
        }
        
        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND book_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantity);
            stmt.setLong(2, userId);
            stmt.setLong(3, bookId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Удаляет товар из корзины.
     */
    public boolean removeFromCart(Long userId, Long bookId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ? AND book_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Очищает всю корзину пользователя.
     */
    public void clearCart(Long userId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Получает общее количество товаров в корзине.
     */
    public int getCartItemCount(Long userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Получает общую сумму корзины.
     */
    public BigDecimal getCartTotal(Long userId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(b.price * c.quantity), 0) 
            FROM cart c
            JOIN books b ON c.book_id = b.id
            WHERE c.user_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        
        return BigDecimal.ZERO;
    }
}
