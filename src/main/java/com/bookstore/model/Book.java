package com.bookstore.model;

import java.math.BigDecimal;

/**
 * Сущность Book представляет книгу в каталоге магазина.
 * Включает полную информацию о книге, наличии и рейтинге.
 */
public record Book(
    Long id,
    String title,
    String author,
    String isbn,
    BigDecimal price,
    String description,
    String category,
    int stock,
    String imageUrl,
    int popularity,
    double averageRating,
    int reviewCount
) {
    /**
     * Конструктор для создания новой книги.
     * ID будет сгенерирован базой данных.
     */
    public Book(String title, String author, String isbn, BigDecimal price, 
                String description, String category, int stock, String imageUrl) {
        this(null, title, author, isbn, price, description, category, 
             stock, imageUrl, 0, 0.0, 0);
    }
    
    /**
     * Проверяет наличие книги на складе.
     */
    public boolean isInStock() {
        return stock > 0;
    }
    
    /**
     * Проверяет, доступна ли книга для покупки.
     */
    public boolean isAvailable() {
        return isInStock();
    }
}
