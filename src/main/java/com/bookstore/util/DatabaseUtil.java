package com.bookstore.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * Утилита для управления пулом соединений с базой данных HikariCP.
 * Осуществляет инициализацию и предоставление DataSource.
 */
public class DatabaseUtil {
    
    private static DataSource dataSource;
    private static final Logger logger = LogManager.getLogger(DatabaseUtil.class);
    
    /**
     * Инициализирует DataSource из параметров контекста веб-приложения.
     * Вызывается один раз при запуске приложения.
     */
    public static void initDataSource(ServletContext context) {
        logger.info("initDataSource called");
        if (dataSource != null) {
            logger.warn("dataSource already exists, skipping");
            return;
        }
        
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(context.getInitParameter("db.url"));
        config.setUsername(context.getInitParameter("db.username"));
        config.setPassword(context.getInitParameter("db.password"));
        
        int maxPoolSize = Integer.parseInt(
            context.getInitParameter("db.maxPoolSize")
        );
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // Дополнительные настройки для H2
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        dataSource = new HikariDataSource(config);
        logger.info("HikariDataSource created, calling initDatabaseSchema");
        
        // Инициализация схемы БД
        initDatabaseSchema();
    }
    
    /**
     * Возвращает инициализированный DataSource.
     * 
     * @return DataSource для подключения к базе данных
     * @throws IllegalStateException если DataSource еще не инициализирован
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource не инициализирован");
        }
        return dataSource;
    }
    
    /**
     * Закрывает DataSource и освобождает все соединения.
     * Вызывается при остановке приложения.
     */
    public static void closeDataSource() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.close();
            dataSource = null;
        }
    }
    
    /**
     * Получает соединение из пула.
     * 
     * @return соединение с базой данных
     * @throws SQLException если не удалось получить соединение
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
    
    /**
     * Инициализирует схему базы данных, выполняя SQL скрипт.
     */
    private static void initDatabaseSchema() {
        logger.info("initDatabaseSchema called");
        try (Connection conn = getConnection()) {
            logger.info("Got connection, creating tables...");
            try (Statement stmt = conn.createStatement()) {
                // Таблица пользователей
                stmt.execute("CREATE TABLE IF NOT EXISTS users (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, email VARCHAR(255) UNIQUE NOT NULL, password_hash VARCHAR(255) NOT NULL, role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER', name VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
                
                // Таблица книг
                stmt.execute("CREATE TABLE IF NOT EXISTS books (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, title VARCHAR(500) NOT NULL, author VARCHAR(500) NOT NULL, isbn VARCHAR(50) UNIQUE NOT NULL, price DECIMAL(10, 2) NOT NULL CHECK (price >= 0), description TEXT, category VARCHAR(255), stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0), image_url VARCHAR(1000), popularity INTEGER NOT NULL DEFAULT 0, average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0, review_count INTEGER NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_category ON books(category)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_price ON books(price)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_popularity ON books(popularity DESC)");
                
                // Таблица корзины
                stmt.execute("CREATE TABLE IF NOT EXISTS cart (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE, quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, UNIQUE(user_id, book_id))");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id)");
                
                // Таблица заказов
                stmt.execute("CREATE TABLE IF NOT EXISTS orders (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, status VARCHAR(50) NOT NULL DEFAULT 'PENDING', total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0), order_number VARCHAR(50) UNIQUE NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC)");
                
                // Таблица позиций заказа
                stmt.execute("CREATE TABLE IF NOT EXISTS order_items (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE, book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE, quantity INTEGER NOT NULL CHECK (quantity > 0), price DECIMAL(10, 2) NOT NULL CHECK (price >= 0), total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0))");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_order_items_book_id ON order_items(book_id)");
                
                // Таблица отзывов
                stmt.execute("CREATE TABLE IF NOT EXISTS reviews (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE, book_title VARCHAR(500) NOT NULL, user_name VARCHAR(255) NOT NULL, rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5), text TEXT, status VARCHAR(50) NOT NULL DEFAULT 'PENDING_MODERATION', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, UNIQUE(user_id, book_id))");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews(book_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status)");
                
                // Таблица списков желаний
                stmt.execute("CREATE TABLE IF NOT EXISTS wishlists (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, name VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_wishlists_user_id ON wishlists(user_id)");
                
                // Таблица товаров в списке желаний
                stmt.execute("CREATE TABLE IF NOT EXISTS wishlist_items (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, wishlist_id BIGINT NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE, book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, UNIQUE(wishlist_id, book_id))");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_wishlist_items_wishlist_id ON wishlist_items(wishlist_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_wishlist_items_book_id ON wishlist_items(book_id)");
                
                // Таблица аудита
                stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, admin_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL, admin_user_name VARCHAR(255), action VARCHAR(100) NOT NULL, entity_type VARCHAR(100) NOT NULL, entity_id BIGINT, details TEXT, timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
                
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_admin_user_id ON audit_logs(admin_user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp DESC)");
                
                // Вставка тестового администратора
                String adminPasswordHash = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
                stmt.execute("INSERT INTO users (email, password_hash, role, name) VALUES ('admin@bookstore.by', '" + adminPasswordHash + "', 'ADMIN', 'Администратор')");
                
                // Вставка тестовых книг (цены в BYN)
                stmt.execute("INSERT INTO books (title, author, isbn, price, description, category, stock, image_url) VALUES " +
                    "('Мастер и Маргарита', 'Михаил Булгаков', '978-5-17-095678-1', 22.50, 'Роман-мистификация', 'Художественная литература', 25, 'https://images.example.com/master-margarita.jpg')," +
                    "('Преступление и наказание', 'Фёдор Достоевский', '978-5-17-095679-8', 19.80, 'Психологический роман', 'Художественная литература', 30, 'https://images.example.com/prestuplenie.jpg')," +
                    "('Гарри Поттер и философский камень', 'Дж. К. Роулинг', '978-5-17-095680-4', 29.50, 'Первая книга о волшебнике', 'Фантастика', 50, 'https://images.example.com/harry-potter.jpg')," +
                    "('Атлант расправил плечи', 'Айн Рэнд', '978-5-17-095681-1', 39.00, 'Философский роман', 'Философия', 15, 'https://images.example.com/atlant.jpg')," +
                    "('Чистый код', 'Роберт Мартин', '978-5-17-095682-8', 49.50, 'Руководство по качеству кода', 'Программирование', 40, 'https://images.example.com/clean-code.jpg')," +
                    "('Совершенный код', 'Стив Макконнелл', '978-5-17-095683-5', 45.00, 'Разработка ПО', 'Программирование', 35, 'https://images.example.com/code-complete.jpg')," +
                    "('Война и мир', 'Лев Толстой', '978-5-17-095684-2', 36.50, 'Эпический роман', 'Художественная литература', 20, 'https://images.example.com/voyna-mir.jpg')," +
                    "('1984', 'Джордж Оруэлл', '978-5-17-095685-9', 18.50, 'Антиутопия', 'Фантастика', 45, 'https://images.example.com/1984.jpg')");
            }
            logger.info("Database schema initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing database schema: " + e.getMessage(), e);
        }
    }
}
