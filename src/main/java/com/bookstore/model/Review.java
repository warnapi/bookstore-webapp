package com.bookstore.model;

import java.time.LocalDateTime;

/**
 * Сущность Review представляет отзыв пользователя о книге.
 * Включает оценку, текст и статус модерации.
 */
public record Review(
    Long id,
    Long userId,
    Long bookId,
    String bookTitle,
    String userName,
    int rating,
    String text,
    ReviewStatus status,
    LocalDateTime createdAt
) {
    /**
     * Конструктор для создания нового отзыва.
     * ID, createdAt и status будут установлены базой данных.
     */
    public Review(Long userId, Long bookId, String bookTitle, 
                  String userName, int rating, String text) {
        this(null, userId, bookId, bookTitle, userName, rating, text, 
             ReviewStatus.PENDING_MODERATION, null);
    }
    
    /**
     * Проверяет, одобрен ли отзыв.
     */
    public boolean isApproved() {
        return status == ReviewStatus.APPROVED;
    }
    
    /**
     * Проверяет, ожидает ли отзыв модерации.
     */
    public boolean isPendingModeration() {
        return status == ReviewStatus.PENDING_MODERATION;
    }
}
