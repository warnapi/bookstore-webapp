package com.bookstore.service;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для административных операций.
 */
public class AdminService {
    
    private static final Logger logger = LogManager.getLogger(AdminService.class);
    private final UserDao userDao;
    private final BookDao bookDao;
    private final OrderDao orderDao;
    private final AuditDao auditDao;
    
    public AdminService() {
        this.userDao = new UserDao();
        this.bookDao = new BookDao();
        this.orderDao = new OrderDao();
        this.auditDao = new AuditDao();
    }
    
    /**
     * Получает статистику продаж за период.
     */
    public SalesStatistics getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) throws ServiceException {
        try {
            long orderCount = orderDao.countByDateRange(startDate, endDate);
            java.math.BigDecimal totalSales = orderDao.getTotalSales();
            long totalOrders = orderDao.count();
            
            return new SalesStatistics(orderCount, totalSales, totalOrders);
            
        } catch (SQLException e) {
            logger.error("Ошибка получения статистики: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения статистики", e);
        }
    }
    
    /**
     * Получает все записи аудита.
     */
    public List<AuditLog> getAuditLogs(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return auditDao.findAll(offset, pageSize);
        } catch (SQLException e) {
            logger.error("Ошибка получения логов аудита: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения логов аудита", e);
        }
    }
    
    /**
     * Записывает действие администратора.
     */
    public void logAdminAction(Long adminUserId, String adminUserName, String action,
                               String entityType, Long entityId, String details) throws ServiceException {
        try {
            AuditLog auditLog = new AuditLog(
                adminUserId, adminUserName, action, entityType, entityId, details
            );
            auditDao.create(auditLog);
            
            logger.info("Действие администратора записано: userId={}, action={}, entity={}", 
                       adminUserId, action, entityType);
            
        } catch (SQLException e) {
            logger.error("Ошибка записи действия администратора: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка записи действия администратора", e);
        }
    }
    
    /**
     * Получает статистику пользователей.
     */
    public UserStatistics getUserStatistics() throws ServiceException {
        try {
            long totalUsers = userDao.count();
            // В реальной системе нужно считать пользователей по ролям
            return new UserStatistics(totalUsers);
            
        } catch (SQLException e) {
            logger.error("Ошибка получения статистики пользователей: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения статистики пользователей", e);
        }
    }
    
    /**
     * Получает статистику книг.
     */
    public BookStatistics getBookStatistics() throws ServiceException {
        try {
            long totalBooks = bookDao.count();
            return new BookStatistics(totalBooks);
            
        } catch (SQLException e) {
            logger.error("Ошибка получения статистики книг: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения статистики книг", e);
        }
    }
    
    /**
     * Внутренний класс для статистики продаж.
     */
    public record SalesStatistics(long orderCount, java.math.BigDecimal totalSales, long totalOrders) {}
    
    /**
     * Внутренний класс для статистики пользователей.
     */
    public record UserStatistics(long totalUsers) {}
    
    /**
     * Внутренний класс для статистики книг.
     */
    public record BookStatistics(long totalBooks) {}
}
