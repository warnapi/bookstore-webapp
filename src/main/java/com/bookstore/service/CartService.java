package com.bookstore.service;

import com.bookstore.dao.CartDao;
import com.bookstore.model.CartItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Сервис для управления корзиной покупок.
 */
public class CartService {
    
    private static final Logger logger = LogManager.getLogger(CartService.class);
    private final CartDao cartDao;
    
    public CartService() {
        this.cartDao = new CartDao();
    }
    
    /**
     * Добавляет товар в корзину.
     */
    public CartItem addToCart(Long userId, Long bookId, int quantity) throws ServiceException {
        try {
            CartItem item = cartDao.addToCart(userId, bookId, quantity);
            
            if (!item.isAvailable()) {
                logger.warn("Добавление недоступного товара в корзину: userId={}, bookId={}", 
                           userId, bookId);
            }
            
            return item;
            
        } catch (SQLException e) {
            logger.error("Ошибка добавления в корзину: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка добавления в корзину", e);
        }
    }
    
    /**
     * Получает все товары в корзине пользователя.
     */
    public List<CartItem> getCartItems(Long userId) throws ServiceException {
        try {
            return cartDao.getCartItems(userId);
        } catch (SQLException e) {
            logger.error("Ошибка получения корзины: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения корзины", e);
        }
    }
    
    /**
     * Обновляет количество товара в корзине.
     */
    public boolean updateQuantity(Long userId, Long bookId, int quantity) throws ServiceException {
        try {
            boolean updated = cartDao.updateQuantity(userId, bookId, quantity);
            
            if (updated) {
                logger.info("Количество товара обновлено: userId={}, bookId={}, quantity={}", 
                           userId, bookId, quantity);
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Ошибка обновления количества: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка обновления количества", e);
        }
    }
    
    /**
     * Удаляет товар из корзины.
     */
    public boolean removeFromCart(Long userId, Long bookId) throws ServiceException {
        try {
            boolean removed = cartDao.removeFromCart(userId, bookId);
            
            if (removed) {
                logger.info("Товар удалён из корзины: userId={}, bookId={}", userId, bookId);
            }
            
            return removed;
            
        } catch (SQLException e) {
            logger.error("Ошибка удаления товара: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка удаления товара", e);
        }
    }
    
    /**
     * Очищает корзину пользователя.
     */
    public void clearCart(Long userId) throws ServiceException {
        try {
            cartDao.clearCart(userId);
            logger.info("Корзина очищена: userId={}", userId);
        } catch (SQLException e) {
            logger.error("Ошибка очистки корзины: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка очистки корзины", e);
        }
    }
    
    /**
     * Получает общее количество товаров в корзине.
     */
    public int getCartItemCount(Long userId) throws ServiceException {
        try {
            return cartDao.getCartItemCount(userId);
        } catch (SQLException e) {
            logger.error("Ошибка получения количества товаров: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения количества товаров", e);
        }
    }
    
    /**
     * Получает общую сумму корзины.
     */
    public BigDecimal getCartTotal(Long userId) throws ServiceException {
        try {
            return cartDao.getCartTotal(userId);
        } catch (SQLException e) {
            logger.error("Ошибка получения общей суммы: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения общей суммы", e);
        }
    }
    
    /**
     * Проверяет, есть ли товары в корзине с недостаточным количеством.
     */
    public boolean hasUnavailableItems(Long userId) throws ServiceException {
        try {
            List<CartItem> items = cartDao.getCartItems(userId);
            return items.stream().anyMatch(item -> !item.isAvailable());
        } catch (SQLException e) {
            logger.error("Ошибка проверки доступности товаров: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка проверки доступности товаров", e);
        }
    }
}
