package com.bookstore.dao;

import com.bookstore.model.Book;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DAO для работы с книгами.
 * Обеспечивает CRUD операции, поиск и фильтрацию.
 */
public class BookDao extends BaseDao {
    
    /**
     * Создаёт новую книгу в базе данных.
     */
    public Book create(Book book) throws SQLException {
        String sql = """
            INSERT INTO books (title, author, isbn, price, description, 
                              category, stock, image_url) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, popularity, average_rating, review_count
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, book.title());
            stmt.setString(2, book.author());
            stmt.setString(3, book.isbn());
            stmt.setBigDecimal(4, book.price());
            stmt.setString(5, book.description());
            stmt.setString(6, book.category());
            stmt.setInt(7, book.stock());
            stmt.setString(8, book.imageUrl());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        
        throw new SQLException("Не удалось создать книгу");
    }
    
    /**
     * Находит книгу по ID.
     */
    public Optional<Book> findById(Long id) throws SQLException {
        String sql = """
            SELECT id, title, author, isbn, price, description, 
                   category, stock, image_url, popularity, 
                   average_rating, review_count
            FROM books WHERE id = ?
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
     * Обновляет данные книги.
     */
    public boolean update(Book book) throws SQLException {
        String sql = """
            UPDATE books SET title = ?, author = ?, isbn = ?, price = ?, 
                            description = ?, category = ?, stock = ?, 
                            image_url = ?
            WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, book.title());
            stmt.setString(2, book.author());
            stmt.setString(3, book.isbn());
            stmt.setBigDecimal(4, book.price());
            stmt.setString(5, book.description());
            stmt.setString(6, book.category());
            stmt.setInt(7, book.stock());
            stmt.setString(8, book.imageUrl());
            stmt.setLong(9, book.id());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Уменьшает количество книги на складе.
     */
    public boolean decreaseStock(Long bookId, int quantity) throws SQLException {
        String sql = """
            UPDATE books SET stock = stock - ? 
            WHERE id = ? AND stock >= ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantity);
            stmt.setLong(2, bookId);
            stmt.setInt(3, quantity);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Увеличивает количество книги на складе.
     */
    public boolean increaseStock(Long bookId, int quantity) throws SQLException {
        String sql = """
            UPDATE books SET stock = stock + ? WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantity);
            stmt.setLong(2, bookId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Удаляет книгу из базы данных.
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Получает все книги с пагинацией.
     */
    public List<Book> findAll(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, title, author, isbn, price, description, 
                   category, stock, image_url, popularity, 
                   average_rating, review_count
            FROM books ORDER BY popularity DESC LIMIT ? OFFSET ?
            """;
        
        return executeQuery(sql, limit, offset);
    }
    
    /**
     * Выполняет полнотекстовый поиск книг.
     * Ищет по названию, автору и описанию.
     */
    public List<Book> search(String query, int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, title, author, isbn, price, description, 
                   category, stock, image_url, popularity, 
                   average_rating, review_count
            FROM books 
            WHERE to_tsvector('russian', title || ' ' || author || ' ' || description) 
                  @@ to_tsquery('russian', ?)
            ORDER BY popularity DESC 
            LIMIT ? OFFSET ?
            """;
        
        // Преобразуем поисковый запрос в формат tsquery
        String tsQuery = Arrays.stream(query.toLowerCase().split("\\s+"))
            .filter(s -> !s.isEmpty())
            .map(s -> s + ":*")
            .collect(Collectors.joining(" & "));
        
        return executeQuery(sql, tsQuery, limit, offset);
    }
    
    /**
     * Фильтрует книги по категории и цене.
     */
    public List<Book> filter(String category, BigDecimal minPrice, BigDecimal maxPrice,
                            int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT id, title, author, isbn, price, description, 
                   category, stock, image_url, popularity, 
                   average_rating, review_count
            FROM books WHERE 1=1
            """);
        
        List<Object> params = new ArrayList<>();
        
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ? ");
            params.add(category);
        }
        
        if (minPrice != null) {
            sql.append(" AND price >= ? ");
            params.add(minPrice);
        }
        
        if (maxPrice != null) {
            sql.append(" AND price <= ? ");
            params.add(maxPrice);
        }
        
        sql.append(" ORDER BY popularity DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            List<Book> books = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
            return books;
        }
    }
    
    /**
     * Получает общее количество книг.
     */
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books";
        
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
     * Получает количество книг по поисковому запросу.
     */
    public long countBySearch(String query) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM books 
            WHERE to_tsvector('russian', title || ' ' || author || ' ' || description) 
                  @@ to_tsquery('russian', ?)
            """;
        
        String tsQuery = Arrays.stream(query.toLowerCase().split("\\s+"))
            .filter(s -> !s.isEmpty())
            .map(s -> s + ":*")
            .collect(Collectors.joining(" & "));
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tsQuery);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Получает все уникальные категории книг.
     */
    public List<String> findAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM books ORDER BY category";
        
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        
        return categories;
    }
    
    /**
     * Проверяет, существует ли книга с данным ISBN.
     */
    public boolean existsByIsbn(String isbn, Long excludeId) throws SQLException {
        String sql = excludeId == null 
            ? "SELECT COUNT(*) FROM books WHERE isbn = ?"
            : "SELECT COUNT(*) FROM books WHERE isbn = ? AND id != ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            if (excludeId != null) {
                stmt.setLong(2, excludeId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Получает популярные книги.
     */
    public List<Book> findPopular(int limit) throws SQLException {
        String sql = """
            SELECT id, title, author, isbn, price, description, 
                   category, stock, image_url, popularity, 
                   average_rating, review_count
            FROM books ORDER BY popularity DESC LIMIT ?
            """;
        
        return executeQuery(sql, limit);
    }
    
    /**
     * Увеличивает счётчик популярности книги.
     */
    public void increasePopularity(Long bookId) throws SQLException {
        String sql = "UPDATE books SET popularity = popularity + 1 WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bookId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Обновляет средний рейтинг книги.
     */
    public void updateAverageRating(Long bookId, double averageRating, int reviewCount) throws SQLException {
        String sql = """
            UPDATE books SET average_rating = ?, review_count = ? WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, averageRating);
            stmt.setInt(2, reviewCount);
            stmt.setLong(3, bookId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Выполняет запрос и возвращает список книг.
     */
    private List<Book> executeQuery(String sql, Object... params) throws SQLException {
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        }
        
        return books;
    }
    
    /**
     * Маппинг строки ResultSet в объект Book.
     */
    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("isbn"),
            rs.getBigDecimal("price"),
            rs.getString("description"),
            rs.getString("category"),
            rs.getInt("stock"),
            rs.getString("image_url"),
            rs.getInt("popularity"),
            rs.getDouble("average_rating"),
            rs.getInt("review_count")
        );
    }
}
