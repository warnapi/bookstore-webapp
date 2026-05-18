package com.bookstore.service;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления отзывами и рейтингами.
 */
public class ReviewService {
    
    private static final Logger logger = LogManager.getLogger(ReviewService.class);
    private final ReviewDao reviewDao;
    private final OrderDao orderDao;
    private final BookDao bookDao;
    
    public ReviewService() {
        this.reviewDao = new ReviewDao();
        this.orderDao = new OrderDao();
        this.bookDao = new BookDao();
    }
    
    /**
     * Проверяет, может ли пользователь оставить отзыв о книге.
     * Отзыв можно оставить только после покупки.
     */
    public boolean canUserReviewBook(Long userId, Long bookId) throws ServiceException {
        try {
            // Проверяем, есть ли уже отзыв
            if (reviewDao.hasUserReviewedBook(userId, bookId)) {
                return false;
            }
            
            // Проверяем, покупал ли пользователь книгу
            // Упрощённая проверка - ищем в заказах
            // В реальной системе нужна более сложная логика
            List<com.bookstore.model.Order> orders = orderDao.findAll(0, 1000);
            
            for (com.bookstore.model.Order order : orders) {
                if (order.userId().equals(userId) && order.status() == OrderStatus.DELIVERED) {
                    List<OrderItem> items = orderDao.findOrderItems(order.id());
                    for (OrderItem item : items) {
                        if (item.bookId().equals(bookId)) {
                            return true;
                        }
                    }
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Ошибка проверки возможности отзыва: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка проверки возможности отзыва", e);
        }
    }
    
    /**
     * Создаёт новый отзыв.
     */
    public Review createReview(Long userId, Long bookId, String bookTitle, String userName, 
                               int rating, String text) throws ServiceException {
        try {
            // Проверка возможности отзыва
            if (!canUserReviewBook(userId, bookId)) {
                throw new ServiceException("Вы можете оставить отзыв только о купленной книге и только один раз");
            }
            
            // Проверка оценки
            if (rating < 1 || rating > 5) {
                throw new ServiceException("Оценка должна быть от 1 до 5");
            }
            
            Review review = new Review(userId, bookId, bookTitle, userName, rating, text);
            Review createdReview = reviewDao.create(review);
            
            // Обновляем средний рейтинг книги
            updateBookRating(bookId);
            
            logger.info("Отзыв создан: userId={}, bookId={}, rating={}", userId, bookId, rating);
            return createdReview;
            
        } catch (SQLException e) {
            logger.error("Ошибка создания отзыва: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка создания отзыва", e);
        }
    }
    
    /**
     * Находит отзыв по ID.
     */
    public Optional<Review> findReviewById(Long id) throws ServiceException {
        try {
            return reviewDao.findById(id);
        } catch (SQLException e) {
            logger.error("Ошибка поиска отзыва: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка поиска отзыва", e);
        }
    }
    
    /**
     * Получает все одобренные отзывы для книги.
     */
    public List<Review> getBookReviews(Long bookId) throws ServiceException {
        try {
            return reviewDao.findByBookId(bookId);
        } catch (SQLException e) {
            logger.error("Ошибка получения отзывов книги: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения отзывов", e);
        }
    }
    
    /**
     * Получает все отзывы пользователя.
     */
    public List<Review> getUserReviews(Long userId) throws ServiceException {
        try {
            return reviewDao.findByUserId(userId);
        } catch (SQLException e) {
            logger.error("Ошибка получения отзывов пользователя: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения отзывов", e);
        }
    }
    
    /**
     * Получает все отзывы, ожидающие модерации.
     */
    public List<Review> getPendingReviews() throws ServiceException {
        try {
            return reviewDao.findPendingForModeration();
        } catch (SQLException e) {
            logger.error("Ошибка получения отзывов для модерации: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения отзывов", e);
        }
    }
    
    /**
     * Одобряет отзыв.
     */
    public boolean approveReview(Long reviewId, Long adminUserId, String adminUserName) throws ServiceException {
        try {
            boolean updated = reviewDao.updateStatus(reviewId, ReviewStatus.APPROVED);
            
            if (updated) {
                Optional<Review> reviewOpt = reviewDao.findById(reviewId);
                if (reviewOpt.isPresent()) {
                    Review review = reviewOpt.get();
                    
                    // Обновляем средний рейтинг книги
                    updateBookRating(review.bookId());
                    
                    // Записываем в аудит
                    com.bookstore.model.AuditLog auditLog = new com.bookstore.model.AuditLog(
                        adminUserId, adminUserName, "APPROVE_REVIEW",
                        "REVIEW", reviewId, "Отзыв одобрен"
                    );
                    new AuditDao().create(auditLog);
                    
                    logger.info("Отзыв одобрен: reviewId={}", reviewId);
                }
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Ошибка одобрения отзыва: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка одобрения отзыва", e);
        }
    }
    
    /**
     * Скрывает отзыв.
     */
    public boolean hideReview(Long reviewId, Long adminUserId, String adminUserName) throws ServiceException {
        try {
            boolean updated = reviewDao.updateStatus(reviewId, ReviewStatus.HIDDEN);
            
            if (updated) {
                Optional<Review> reviewOpt = reviewDao.findById(reviewId);
                if (reviewOpt.isPresent()) {
                    Review review = reviewOpt.get();
                    
                    // Обновляем средний рейтинг книги
                    updateBookRating(review.bookId());
                    
                    // Записываем в аудит
                    com.bookstore.model.AuditLog auditLog = new com.bookstore.model.AuditLog(
                        adminUserId, adminUserName, "HIDE_REVIEW",
                        "REVIEW", reviewId, "Отзыв скрыт"
                    );
                    new AuditDao().create(auditLog);
                    
                    logger.info("Отзыв скрыт: reviewId={}", reviewId);
                }
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Ошибка скрытия отзыва: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка скрытия отзыва", e);
        }
    }
    
    /**
     * Удаляет отзыв (администратор).
     */
    public boolean deleteReview(Long reviewId, Long adminUserId, String adminUserName) throws ServiceException {
        try {
            Optional<Review> reviewOpt = reviewDao.findById(reviewId);
            
            if (reviewOpt.isPresent()) {
                Review review = reviewOpt.get();
                
                boolean deleted = reviewDao.delete(reviewId);
                
                if (deleted) {
                    // Обновляем средний рейтинг книги
                    updateBookRating(review.bookId());
                    
                    // Записываем в аудит
                    com.bookstore.model.AuditLog auditLog = new com.bookstore.model.AuditLog(
                        adminUserId, adminUserName, "DELETE_REVIEW",
                        "REVIEW", reviewId, "Отзыв удалён"
                    );
                    new AuditDao().create(auditLog);
                    
                    logger.info("Отзыв удалён администратором: reviewId={}", reviewId);
                }
                
                return deleted;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Ошибка удаления отзыва: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка удаления отзыва", e);
        }
    }
    
    /**
     * Удаляет отзыв пользователем.
     */
    public boolean deleteReview(Long reviewId, Long userId) throws ServiceException {
        try {
            Optional<Review> reviewOpt = reviewDao.findById(reviewId);
            
            if (reviewOpt.isPresent()) {
                Review review = reviewOpt.get();
                
                // Проверяем, что отзыв принадлежит пользователю
                if (!review.userId().equals(userId)) {
                    throw new ServiceException("Вы не можете удалить чужой отзыв");
                }
                
                boolean deleted = reviewDao.delete(reviewId);
                
                if (deleted) {
                    // Обновляем средний рейтинг книги
                    updateBookRating(review.bookId());
                    
                    logger.info("Отзыв удалён пользователем: reviewId={}, userId={}", reviewId, userId);
                }
                
                return deleted;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Ошибка удаления отзыва пользователем: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка удаления отзыва", e);
        }
    }
    
    /**
     * Обновляет средний рейтинг книги.
     */
    private void updateBookRating(Long bookId) throws SQLException {
        double averageRating = reviewDao.getAverageRating(bookId);
        long reviewCount = reviewDao.countByBookId(bookId);
        
        bookDao.updateAverageRating(bookId, averageRating, (int) reviewCount);
    }
}
