-- Создание базы данных
-- CREATE DATABASE bookstore;

-- Переключение на базу данных
-- \c bookstore

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для быстрого поиска по email
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Таблица книг
CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(500) NOT NULL,
    isbn VARCHAR(50) UNIQUE NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    description TEXT,
    category VARCHAR(255),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    image_url VARCHAR(1000),
    popularity INTEGER NOT NULL DEFAULT 0,
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0,
    review_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для поиска и фильтрации
CREATE INDEX IF NOT EXISTS idx_books_title ON books USING gin(to_tsvector('russian', title));
CREATE INDEX IF NOT EXISTS idx_books_author ON books USING gin(to_tsvector('russian', author));
CREATE INDEX IF NOT EXISTS idx_books_category ON books(category);
CREATE INDEX IF NOT EXISTS idx_books_price ON books(price);
CREATE INDEX IF NOT EXISTS idx_books_popularity ON books(popularity DESC);

-- Таблица корзины
CREATE TABLE IF NOT EXISTS cart (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id)
);

-- Индекс для быстрого поиска корзины пользователя
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Генерация уникального номера заказа
CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.order_number := 'ORD-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD') || '-' || LPAD(NEW.id::TEXT, 6, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_order_number
    BEFORE INSERT ON orders
    FOR EACH ROW
    EXECUTE FUNCTION generate_order_number();

-- Индексы для заказов
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

-- Таблица позиций заказа
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0)
);

-- Индексы для позиций заказа
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_book_id ON order_items(book_id);

-- Таблица отзывов
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    book_title VARCHAR(500) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    text TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_MODERATION',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id)
);

-- Индексы для отзывов
CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews(book_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status);

-- Таблица списков желаний
CREATE TABLE IF NOT EXISTS wishlists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для списков желаний пользователя
CREATE INDEX IF NOT EXISTS idx_wishlists_user_id ON wishlists(user_id);

-- Таблица товаров в списке желаний
CREATE TABLE IF NOT EXISTS wishlist_items (
    id BIGSERIAL PRIMARY KEY,
    wishlist_id BIGINT NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(wishlist_id, book_id)
);

-- Индексы для товаров списка желаний
CREATE INDEX IF NOT EXISTS idx_wishlist_items_wishlist_id ON wishlist_items(wishlist_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_items_book_id ON wishlist_items(book_id);

-- Таблица аудита (для администраторов)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    admin_user_name VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для аудита
CREATE INDEX IF NOT EXISTS idx_audit_logs_admin_user_id ON audit_logs(admin_user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp DESC);

-- Вставка тестового администратора
-- Пароль: admin123 (хеширован с помощью BCrypt)
INSERT INTO users (email, password_hash, role, name) VALUES
('admin@bookstore.by', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYILp92S.0i', 'ADMIN', 'Администратор')
ON CONFLICT (email) DO NOTHING;

-- Вставка тестовых книг
INSERT INTO books (title, author, isbn, price, description, category, stock, image_url) VALUES
('Мастер и Маргарита', 'Михаил Булгаков', '978-5-17-095678-1', 550.00, 
 'Роман-мистификация, насыщенная фантастическими и сатирическими элементами', 'Художественная литература', 25, 
 'https://images.example.com/master-margarita.jpg'),

('Преступление и наказание', 'Фёдор Достоевский', '978-5-17-095679-8', 480.00,
 'Психологический роман о нравственных муках молодого человека', 'Художественная литература', 30,
 'https://images.example.com/prestuplenie.jpg'),

('Гарри Поттер и философский камень', 'Дж. К. Роулинг', '978-5-17-095680-4', 720.00,
 'Первая книга о мальчике-волшебнике', 'Фантастика', 50,
 'https://images.example.com/harry-potter.jpg'),

('Атлант расправил плечи', 'Айн Рэнд', '978-5-17-095681-1', 950.00,
 'Философский роман об идеях объективизма', 'Философия', 15,
 'https://images.example.com/atlant.jpg'),

('Чистый код', 'Роберт Мартин', '978-5-17-095682-8', 1200.00,
 'Руководство по созданию качественного программного кода', 'Программирование', 40,
 'https://images.example.com/clean-code.jpg'),

('Совершенный код', 'Стив Макконнелл', '978-5-17-095683-5', 1100.00,
 'Практическое руководство по разработке программного обеспечения', 'Программирование', 35,
 'https://images.example.com/code-complete.jpg'),

('Война и мир', 'Лев Толстой', '978-5-17-095684-2', 890.00,
 'Эпический роман о войне 1812 года', 'Художественная литература', 20,
 'https://images.example.com/voyna-mir.jpg'),

('1984', 'Джордж Оруэлл', '978-5-17-095685-9', 450.00,
 'Антиутопия о тоталитарном режиме', 'Фантастика', 45,
 'https://images.example.com/1984.jpg');

-- Создание полного текстового поиска (русский язык)
CREATE EXTENSION IF NOT EXISTS unaccent;
