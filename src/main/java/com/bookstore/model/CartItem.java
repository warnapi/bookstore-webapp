package com.bookstore.model;

import java.math.BigDecimal;

/**
 * Сущность CartItem представляет товар в корзине пользователя.
 * Связывает книгу с количеством и суммой.
 */
public record CartItem(
    Long id,
    Long userId,
    Long bookId,
    String bookTitle,
    String bookAuthor,
    BigDecimal bookPrice,
    String bookImageUrl,
    int quantity,
    int stockAvailable,
    boolean isAvailable
) {
    /**
     * Рассчитывает общую сумму для данного товара.
     */
    public BigDecimal getTotalPrice() {
        return bookPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * Проверяет, может ли быть увеличено количество товара.
     */
    public boolean canIncreaseQuantity() {
        return quantity < stockAvailable;
    }
}
