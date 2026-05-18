package com.bookstore.model;

import java.math.BigDecimal;

/**
 * Сущность OrderItem представляет позицию заказа.
 * Связывает заказ с книгой и количеством.
 */
public record OrderItem(
    Long id,
    Long orderId,
    Long bookId,
    String bookTitle,
    String bookAuthor,
    int quantity,
    BigDecimal price,
    BigDecimal totalPrice
) {
    /**
     * Конструктор для создания новой позиции заказа.
     * ID и totalPrice будут рассчитаны.
     */
    public OrderItem(Long orderId, Long bookId, String bookTitle, 
                     String bookAuthor, int quantity, BigDecimal price) {
        this(null, orderId, bookId, bookTitle, bookAuthor, quantity, 
             price, price.multiply(BigDecimal.valueOf(quantity)));
    }
}
