package com.bookstore.service;

import com.bookstore.dao.UserDao;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Сервис для управления пользователями и аутентификации.
 */
public class AuthService {
    
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private final UserDao userDao;
    
    public AuthService() {
        this.userDao = new UserDao();
    }
    
    /**
     * Регистрирует нового пользователя.
     * 
     * @param email адрес электронной почты
     * @param password пароль
     * @param name имя пользователя
     * @return созданный пользователь
     * @throws ServiceException если регистрация не удалась
     */
    public User register(String email, String password, String name) throws ServiceException {
        try {
            // Проверка на существование пользователя
            if (userDao.existsByEmail(email)) {
                throw new ServiceException("Пользователь с таким email уже существует");
            }
            
            // Хеширование пароля
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            
            // Создание пользователя
            User user = new User(email, passwordHash, Role.CUSTOMER, name);
            User createdUser = userDao.create(user);
            
            logger.info("Пользователь зарегистрирован: {}", createdUser.email());
            return createdUser;
            
        } catch (SQLException e) {
            logger.error("Ошибка регистрации пользователя: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка регистрации пользователя", e);
        }
    }
    
    /**
     * Аутентифицирует пользователя.
     * 
     * @param email адрес электронной почты
     * @param password пароль
     * @return аутентифицированный пользователь
     * @throws ServiceException если аутентификация не удалась
     */
    public User authenticate(String email, String password) throws ServiceException {
        try {
            Optional<User> userOpt = userDao.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                throw new ServiceException("Неверный email или пароль");
            }
            
            User user = userOpt.get();
            
            // Проверка пароля
            if (!BCrypt.checkpw(password, user.passwordHash())) {
                throw new ServiceException("Неверный email или пароль");
            }
            
            logger.info("Пользователь аутентифицирован: {}", user.email());
            return user;
            
        } catch (SQLException e) {
            logger.error("Ошибка аутентификации: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка аутентификации", e);
        }
    }
    
    /**
     * Находит пользователя по ID.
     */
    public Optional<User> findUserById(Long id) throws ServiceException {
        try {
            return userDao.findById(id);
        } catch (SQLException e) {
            logger.error("Ошибка поиска пользователя: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка поиска пользователя", e);
        }
    }
    
    /**
     * Обновляет данные пользователя.
     */
    public User updateUser(Long userId, String email, String name) throws ServiceException {
        try {
            Optional<User> userOpt = userDao.findById(userId);
            
            if (userOpt.isEmpty()) {
                throw new ServiceException("Пользователь не найден");
            }
            
            User user = userOpt.get();
            User updatedUser = new User(
                user.id(), email, user.passwordHash(), 
                user.role(), name, user.createdAt()
            );
            
            userDao.update(updatedUser);
            logger.info("Данные пользователя обновлены: {}", updatedUser.email());
            
            return updatedUser;
            
        } catch (SQLException e) {
            logger.error("Ошибка обновления пользователя: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка обновления пользователя", e);
        }
    }
    
    /**
     * Меняет пароль пользователя.
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) throws ServiceException {
        try {
            Optional<User> userOpt = userDao.findById(userId);
            
            if (userOpt.isEmpty()) {
                throw new ServiceException("Пользователь не найден");
            }
            
            User user = userOpt.get();
            
            // Проверка старого пароля
            if (!BCrypt.checkpw(oldPassword, user.passwordHash())) {
                throw new ServiceException("Неверный старый пароль");
            }
            
            // Хеширование нового пароля
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            
            // Обновление пароля
            boolean updated = userDao.updatePassword(userId, newPasswordHash);
            
            if (updated) {
                logger.info("Пароль изменён для пользователя: {}", user.email());
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Ошибка смены пароля: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка смены пароля", e);
        }
    }
}
