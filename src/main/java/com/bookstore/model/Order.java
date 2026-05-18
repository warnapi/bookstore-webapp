package com.bookstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность Order представляет заказ пользователя.
 * Включает информацию о статусе, сумме и дате создания.
 */
public record Order(
    Long id,
    Long userId,
    OrderStatus status,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    String orderNumber
) {
    /**
     * Конструктор для создания нового заказа.
     * ID, createdAt и orderNumber будут установлены базой данных.
     */
    public Order(Long userId, OrderStatus status, BigDecimal totalAmount) {
        this(null, userId, status, totalAmount, null, null);
    }
    
    /**
     * Проверяет, может ли заказ быть отменён.
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }
}
