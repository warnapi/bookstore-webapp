package com.bookstore.model;

import java.time.LocalDateTime;

/**
 * Сущность User представляет пользователя интернет-магазина.
 * Включает информацию об учетной записи, роль и личные данные.
 */
public record User(
    Long id,
    String email,
    String passwordHash,
    Role role,
    String name,
    LocalDateTime createdAt
) {
    /**
     * Конструктор для создания нового пользователя.
     * ID и createdAt будут установлены базой данных.
     */
    public User(String email, String passwordHash, Role role, String name) {
        this(null, email, passwordHash, role, name, null);
    }
    
    /**
     * Проверяет, является ли пользователь администратором.
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}

