package com.bookstore.model;

/**
 * Сущность Wishlist представляет список желаний пользователя.
 * Может содержать несколько книг.
 */
public record Wishlist(
    Long id,
    Long userId,
    String name
) {
    /**
     * Конструктор для создания нового списка желаний.
     * ID будет сгенерирован базой данных.
     */
    public Wishlist(Long userId, String name) {
        this(null, userId, name);
    }
}
