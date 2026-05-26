package com.bookstore.model;

/**
 * Сущность Wishlist представляет список желаний пользователя.
 * Может содержать несколько книг.
 */
public record Wishlist(
    Long id,
    Long userId,
    String name,
    int itemCount
) {
    /**
     * Конструктор для создания нового списка желаний.
     * ID будет сгенерирован базой данных.
     */
    public Wishlist(Long userId, String name) {
        this(null, userId, name, 0);
    }
    
    /**
     * Конструктор для создания нового списка желаний с ID.
     */
    public Wishlist(Long id, Long userId, String name) {
        this(id, userId, name, 0);
    }
}
