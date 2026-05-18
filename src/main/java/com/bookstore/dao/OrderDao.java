package com.bookstore.dao;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.OrderStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с заказами.
 */
public class OrderDao extends BaseDao {
    
    /**
     * Создаёт новый заказ.
     */
    public Order create(Order order) throws SQLException {
        String orderSql = """
            INSERT INTO orders (user_id, status, total_amount, order_number) 
            VALUES (?, ?, ?, ?)
            """;
        
        Long orderId;
        String orderNumber;
        LocalDateTime createdAt;
        
        // Генерируем номер заказа
        orderNumber = generateOrderNumber();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, order.userId());
            stmt.setString(2, order.status().name());
            stmt.setBigDecimal(3, order.totalAmount());
            stmt.setString(4, orderNumber);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Не удалось создать заказ");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    orderId = rs.getLong(1);
                    // created_at генерируется базой данных
                    createdAt = LocalDateTime.now();
                } else {
                    throw new SQLException("Не удалось создать заказ");
                }
            }
        }
        
        return new Order(orderId, order.userId(), order.status(), 
                        order.totalAmount(), createdAt, orderNumber);
    }
    
    /**
     * Генерирует уникальный номер заказа.
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    /**
     * Добавляет позицию в заказ.
     */
    public OrderItem addOrderItem(OrderItem item) throws SQLException {
        String sql = """
            INSERT INTO order_items (order_id, book_id, quantity, price, total_price)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, item.orderId());
            stmt.setLong(2, item.bookId());
            stmt.setInt(3, item.quantity());
            stmt.setBigDecimal(4, item.price());
            stmt.setBigDecimal(5, item.totalPrice());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Не удалось добавить позицию в заказ");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new OrderItem(
                        rs.getLong(1),
                        item.orderId(),
                        item.bookId(),
                        item.bookTitle(),
                        item.bookAuthor(),
                        item.quantity(),
                        item.price(),
                        item.totalPrice()
                    );
                }
            }
        }
        
        throw new SQLException("Не удалось добавить позицию в заказ");
    }
    
    /**
     * Находит заказ по ID.
     */
    public Optional<Order> findById(Long id) throws SQLException {
        String sql = """
            SELECT id, user_id, status, total_amount, created_at, order_number
            FROM orders WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapOrderRow(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Находит все заказы пользователя с пагинацией.
     */
    public List<Order> findByUserId(Long userId, int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, user_id, status, total_amount, created_at, order_number
            FROM orders WHERE user_id = ?
            ORDER BY created_at DESC LIMIT ? OFFSET ?
            """;
        
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderRow(rs));
                }
            }
        }
        
        return orders;
    }
    
    /**
     * Получает все заказы с пагинацией (для администратора).
     */
    public List<Order> findAll(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, user_id, status, total_amount, created_at, order_number
            FROM orders ORDER BY created_at DESC LIMIT ? OFFSET ?
            """;
        
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderRow(rs));
                }
            }
        }
        
        return orders;
    }
    
    /**
     * Обновляет статус заказа.
     */
    public boolean updateStatus(Long orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setLong(2, orderId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Удаляет заказ.
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Получает позиции заказа.
     */
    public List<OrderItem> findOrderItems(Long orderId) throws SQLException {
        String sql = """
            SELECT oi.id, oi.order_id, oi.book_id, b.title, b.author, 
                   oi.quantity, oi.price, oi.total_price
            FROM order_items oi
            JOIN books b ON oi.book_id = b.id
            WHERE oi.order_id = ?
            """;
        
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new OrderItem(
                        rs.getLong("id"),
                        rs.getLong("order_id"),
                        rs.getLong("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("price"),
                        rs.getBigDecimal("total_price")
                    ));
                }
            }
        }
        
        return items;
    }
    
    /**
     * Получает общее количество заказов.
     */
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";
        
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
     * Получает количество заказов пользователя.
     */
    public long countByUserId(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Получает общую сумму продаж.
     */
    public BigDecimal getTotalSales() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'PAID'";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Получает количество заказов за период.
     */
    public long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM orders 
            WHERE created_at >= ? AND created_at <= ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Маппинг строки ResultSet в объект Order.
     */
    private Order mapOrderRow(ResultSet rs) throws SQLException {
        return new Order(
            rs.getLong("id"),
            rs.getLong("user_id"),
            OrderStatus.valueOf(rs.getString("status")),
            rs.getBigDecimal("total_amount"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getString("order_number")
        );
    }
}
