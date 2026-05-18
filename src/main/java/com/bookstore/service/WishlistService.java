package com.bookstore.service;

import com.bookstore.dao.WishlistDao;
import com.bookstore.model.Wishlist;
import com.bookstore.model.WishlistItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления списками желаний.
 */
public class WishlistService {
    
    private static final Logger logger = LogManager.getLogger(WishlistService.class);
    private final WishlistDao wishlistDao;
    
    public WishlistService() {
        this.wishlistDao = new WishlistDao();
    }
    
    /**
     * Создаёт новый список желаний.
     */
    public Wishlist createWishlist(Long userId, String name) throws ServiceException {
        try {
            Wishlist wishlist = new Wishlist(userId, name);
            Wishlist createdWishlist = wishlistDao.create(wishlist);
            
            logger.info("Список желаний создан: userId={}, wishlistId={}, name={}", 
                       userId, createdWishlist.id(), name);
            return createdWishlist;
            
        } catch (SQLException e) {
            logger.error("Ошибка создания списка желаний: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка создания списка желаний", e);
        }
    }
    
    /**
     * Находит список желаний по ID.
     */
    public Optional<Wishlist> findWishlistById(Long id) throws ServiceException {
        try {
            return wishlistDao.findById(id);
        } catch (SQLException e) {
            logger.error("Ошибка поиска списка желаний: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка поиска списка желаний", e);
        }
    }
    
    /**
     * Получает все списки желаний пользователя.
     */
    public List<Wishlist> getUserWishlists(Long userId) throws ServiceException {
        try {
            return wishlistDao.findByUserId(userId);
        } catch (SQLException e) {
            logger.error("Ошибка получения списков желаний: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения списков желаний", e);
        }
    }
    
    /**
     * Обновляет имя списка желаний.
     */
    public boolean updateWishlistName(Long wishlistId, String name) throws ServiceException {
        try {
            boolean updated = wishlistDao.updateName(wishlistId, name);
            
            if (updated) {
                logger.info("Имя списка желаний обновлено: wishlistId={}, name={}", wishlistId, name);
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Ошибка обновления имени списка: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка обновления имени списка", e);
        }
    }
    
    /**
     * Удаляет список желаний.
     */
    public boolean deleteWishlist(Long id) throws ServiceException {
        try {
            boolean deleted = wishlistDao.delete(id);
            
            if (deleted) {
                logger.info("Список желаний удалён: wishlistId={}", id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            logger.error("Ошибка удаления списка желаний: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка удаления списка желаний", e);
        }
    }
    
    /**
     * Добавляет книгу в список желаний.
     */
    public WishlistItem addItemToWishlist(Long wishlistId, Long bookId) throws ServiceException {
        try {
            WishlistItem item = wishlistDao.addItem(wishlistId, bookId);
            
            logger.info("Книга добавлена в список желаний: wishlistId={}, bookId={}", 
                       wishlistId, bookId);
            return item;
            
        } catch (SQLException e) {
            logger.error("Ошибка добавления книги в список: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка добавления книги в список", e);
        }
    }
    
    /**
     * Удаляет книгу из списка желаний.
     */
    public boolean removeItemFromWishlist(Long wishlistId, Long bookId) throws ServiceException {
        try {
            boolean removed = wishlistDao.removeItem(wishlistId, bookId);
            
            if (removed) {
                logger.info("Книга удалена из списка желаний: wishlistId={}, bookId={}", 
                           wishlistId, bookId);
            }
            
            return removed;
            
        } catch (SQLException e) {
            logger.error("Ошибка удаления книги из списка: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка удаления книги из списка", e);
        }
    }
    
    /**
     * Получает все товары в списке желаний.
     */
    public List<WishlistItem> getWishlistItems(Long wishlistId) throws ServiceException {
        try {
            return wishlistDao.getItems(wishlistId);
        } catch (SQLException e) {
            logger.error("Ошибка получения товаров списка: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения товаров списка", e);
        }
    }
    
    /**
     * Проверяет, есть ли книга в списке желаний.
     */
    public boolean hasBookInWishlist(Long wishlistId, Long bookId) throws ServiceException {
        try {
            return wishlistDao.hasItem(wishlistId, bookId);
        } catch (SQLException e) {
            logger.error("Ошибка проверки наличия книги: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка проверки наличия книги", e);
        }
    }
}
