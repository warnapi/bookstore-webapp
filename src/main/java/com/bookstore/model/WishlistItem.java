package com.bookstore.model;

import java.math.BigDecimal;

/**
 * Сущность WishlistItem представляет товар в списке желаний.
 * Связывает список желаний с книгой.
 */
public record WishlistItem(
    Long id,
    Long wishlistId,
    Long bookId,
    String bookTitle,
    String bookAuthor,
    BigDecimal bookPrice,
    String bookImageUrl,
    boolean isInStock
) {
    /**
     * Конструктор для создания нового товара в списке желаний.
     * ID будет сгенерирован базой данных.
     */
    public WishlistItem(Long wishlistId, Long bookId, String bookTitle, 
                        String bookAuthor, BigDecimal bookPrice, 
                        String bookImageUrl, boolean isInStock) {
        this(null, wishlistId, bookId, bookTitle, bookAuthor, 
             bookPrice, bookImageUrl, isInStock);
    }
}
