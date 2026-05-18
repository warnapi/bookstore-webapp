package com.bookstore.service;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.util.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


/**
 * Сервис для обработки заказов.
 */
public class OrderService {
    
    private static final Logger logger = LogManager.getLogger(OrderService.class);
    private final OrderDao orderDao;
    private final CartDao cartDao;
    private final BookDao bookDao;
    private final AuditDao auditDao;
    
    public OrderService() {
        this.orderDao = new OrderDao();
        this.cartDao = new CartDao();
        this.bookDao = new BookDao();
        this.auditDao = new AuditDao();
    }
    
    /**
     * Создаёт заказ из корзины пользователя.
     */
    public Order createOrderFromCart(Long userId) throws ServiceException {
        java.sql.Connection conn = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Получаем товары корзины
            List<CartItem> cartItems = cartDao.getCartItems(userId);
            
            if (cartItems.isEmpty()) {
                throw new ServiceException("Корзина пуста");
            }
            
            // Проверяем наличие товаров
            for (CartItem item : cartItems) {
                if (!item.isAvailable()) {
                    throw new ServiceException("Товар " + item.bookTitle() + " больше недоступен");
                }
            }
            
            // Рассчитываем общую сумму
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                totalAmount = totalAmount.add(item.getTotalPrice());
            }
            
            // Создаём заказ
            Order order = new Order(userId, OrderStatus.PENDING, totalAmount);
            order = orderDao.create(order);
            
            // Создаём позиции заказа и уменьшаем запас товаров
            for (CartItem item : cartItems) {
                OrderItem orderItem = new OrderItem(
                    order.id(), item.bookId(), item.bookTitle(), 
                    item.bookAuthor(), item.quantity(), item.bookPrice()
                );
                orderDao.addOrderItem(orderItem);
                
                // Уменьшаем запас книги
                if (!bookDao.decreaseStock(item.bookId(), item.quantity())) {
                    throw new ServiceException("Недостаточно товара: " + item.bookTitle());
                }
            }
            
            // Очищаем корзину
            cartDao.clearCart(userId);
            
            conn.commit();
            logger.info("Заказ создан: orderId={}, userId={}, totalAmount={}", 
                       order.id(), userId, totalAmount);
            
            return order;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Ошибка отката транзакции", ex);
                }
            }
            logger.error("Ошибка создания заказа: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка создания заказа", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Ошибка восстановления автокоммита", e);
                }
            }
        }
    }
    
    /**
     * Находит заказ по ID.
     */
    public Optional<Order> findOrderById(Long id) throws ServiceException {
        try {
            return orderDao.findById(id);
        } catch (SQLException e) {
            logger.error("Ошибка поиска заказа: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка поиска заказа", e);
        }
    }
    
    /**
     * Получает все заказы пользователя.
     */
    public List<Order> getUserOrders(Long userId, int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return orderDao.findByUserId(userId, offset, pageSize);
        } catch (SQLException e) {
            logger.error("Ошибка получения заказов пользователя: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения заказов", e);
        }
    }
    
    /**
     * Получает все заказы (для администратора).
     */
    public List<Order> getAllOrders(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return orderDao.findAll(offset, pageSize);
        } catch (SQLException e) {
            logger.error("Ошибка получения всех заказов: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения заказов", e);
        }
    }
    
    /**
     * Обновляет статус заказа.
     */
    public boolean updateOrderStatus(Long orderId, OrderStatus status, Long adminUserId, String adminUserName) throws ServiceException {
        try {
            boolean updated = orderDao.updateStatus(orderId, status);
            
            if (updated) {
                // Записываем в аудит
                AuditLog auditLog = new AuditLog(
                    adminUserId, adminUserName, "UPDATE_STATUS",
                    "ORDER", orderId, "Статус изменён на: " + status.name()
                );
                auditDao.create(auditLog);
                
                logger.info("Статус заказа обновлён: orderId={}, status={}", orderId, status);
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Ошибка обновления статуса заказа: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка обновления статуса заказа", e);
        }
    }
    
    /**
     * Отменяет заказ.
     */
    public boolean cancelOrder(Long orderId, Long userId) throws ServiceException {
        try {
            Optional<Order> orderOpt = orderDao.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                throw new ServiceException("Заказ не найден");
            }
            
            Order order = orderOpt.get();
            
            if (!order.canBeCancelled()) {
                throw new ServiceException("Заказ нельзя отменить в текущем статусе");
            }
            
            // Проверяем, что заказ принадлежит пользователю
            if (!order.userId().equals(userId)) {
                throw new ServiceException("Заказ не принадлежит пользователю");
            }
            
            // Возвращаем товары на склад
            List<OrderItem> items = orderDao.findOrderItems(orderId);
            for (OrderItem item : items) {
                bookDao.increaseStock(item.bookId(), item.quantity());
            }
            
            // Обновляем статус
            boolean updated = orderDao.updateStatus(orderId, OrderStatus.CANCELLED);
            
            if (updated) {
                logger.info("Заказ отменён: orderId={}, userId={}", orderId, userId);
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Ошибка отмены заказа: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка отмены заказа", e);
        }
    }
    
    /**
     * Получает позиции заказа.
     */
    public List<OrderItem> getOrderItems(Long orderId) throws ServiceException {
        try {
            return orderDao.findOrderItems(orderId);
        } catch (SQLException e) {
            logger.error("Ошибка получения позиций заказа: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения позиций заказа", e);
        }
    }
    
    /**
     * Получает общее количество заказов.
     */
    public long countOrders() throws ServiceException {
        try {
            return orderDao.count();
        } catch (SQLException e) {
            logger.error("Ошибка подсчёта заказов: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка подсчёта заказов", e);
        }
    }
    
    /**
     * Получает общую сумму продаж.
     */
    public BigDecimal getTotalSales() throws ServiceException {
        try {
            return orderDao.getTotalSales();
        } catch (SQLException e) {
            logger.error("Ошибка получения общей суммы продаж: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения общей суммы продаж", e);
        }
    }
}
