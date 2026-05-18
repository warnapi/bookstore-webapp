package com.bookstore.model;

import java.time.LocalDateTime;

/**
 * Сущность AuditLog представляет запись аудита действий администратора.
 * Используется для отслеживания всех изменений в системе.
 */
public record AuditLog(
    Long id,
    Long adminUserId,
    String adminUserName,
    String action,
    String entityType,
    Long entityId,
    String details,
    LocalDateTime timestamp
) {
    /**
     * Конструктор для создания новой записи аудита.
     * ID и timestamp будут установлены базой данных.
     */
    public AuditLog(Long adminUserId, String adminUserName, String action,
                    String entityType, Long entityId, String details) {
        this(null, adminUserId, adminUserName, action, entityType, 
             entityId, details, null);
    }
}
