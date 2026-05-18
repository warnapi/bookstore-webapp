# Интернет-магазин книг (Online Bookstore)

 Jakarta EE веб-приложение для интернет-магазина книг, построенное на Servlets, JSP и JDBC.

## 📋 Требования

- **JDK 25** или выше
- **Apache Maven 3.9+**
- **Apache Tomcat 11.0.21+**
- **PostgreSQL 18.3+**

## 🚀 Быстрый старт

### 1. Настройка базы данных

```bash
# Создайте базу данных
psql -U postgres -c "CREATE DATABASE bookstore;"

# Выполните SQL скрипт для создания таблиц
psql -U postgres -d bookstore -f src/main/resources/schema.sql
```

### 2. Настройка подключения к БД

Отредактируйте `src/main/webapp/WEB-INF/web.xml` и измените параметры подключения:

```xml
<context-param>
    <param-name>db.url</param-name>
    <param-value>jdbc:postgresql://localhost:5432/bookstore</param-value>
</context-param>
<context-param>
    <param-name>db.username</param-name>
    <param-value>ваш_пользователь</param-value>
</context-param>
<context-param>
    <param-name>db.password</param-name>
    <param-value>ваш_пароль</param-value>
</context-param>
```

### 3. Сборка проекта

```bash
mvn clean package
```

### 4. Развёртывание на Tomcat

Скопируйте сгенерированный WAR файл в директорию `webapps` Tomcat:

```bash
cp target/bookstore.war $TOMCAT_HOME/webapps/
```

### 5. Запуск

Запустите Tomcat:

```bash
$TOMCAT_HOME/bin/startup.sh  # Linux/Mac
$TOMCAT_HOME/bin/startup.bat # Windows
```

Откройте в браузере: `http://localhost:8080/bookstore`

## 🔐 Тестовые учётные записи

### Администратор
- **Email:** admin@bookstore.by
- **Пароль:** admin123

### Клиент
Зарегистрируйтесь через форму регистрации на сайте.

## 📁 Структура проекта

```
src/main/
├── java/com/bookstore/
│   ├── model/          # POJO сущности
│   ├── dao/            # Data Access Objects (JDBC)
│   ├── service/        # Бизнес-логика
│   ├── controller/     # Servlets
│   │   └── admin/      # Административные сервлеты
│   ├── filter/         # Фильтры
│   └── util/           # Утилиты
├── webapp/
│   └── WEB-INF/
│       ├── web.xml     # Конфигурация приложения
│       └── views/      # JSP страницы
│           ├── admin/      # Админ-панель
│           ├── catalog/    # Каталог книг
│           ├── auth/       # Авторизация
│           ├── cart/       # Корзина
│           ├── order/      # Заказы
│           ├── user/       # Личный кабинет
│           └── wishlist/   # Списки желаний
└── resources/
    ├── log4j2.xml      # Конфигурация логирования
    └── schema.sql      # SQL скрипт БД
```

## 🎯 Основные функции

### Для клиентов:
- ✅ Просмотр каталога книг
- ✅ Поиск и фильтрация книг
- ✅ Регистрация и авторизация
- ✅ Корзина покупок
- ✅ Оформление заказов
- ✅ История заказов
- ✅ Списки желаний (несколько именованных списков)
- ✅ Отзывы о купленных книгах
- ✅ Личный кабинет

### Для администраторов:
- ✅ Панель управления с аналитикой
- ✅ CRUD управление книгами
- ✅ Управление заказами
- ✅ Управление пользователями
- ✅ Модерация отзывов
- ✅ Аудит действий

## 🛠 Технологии

| Технология | Версия | Описание |
|------------|--------|----------|
| Jakarta Servlet | 6.1 | Обработка HTTP запросов |
| Jakarta JSP | 3.1 | Шаблоны страниц |
| Jakarta JSTL | 3.0 | Стандартные теги JSP |
| PostgreSQL | 18.3 | СУБД |
| JDBC | - | Работа с БД |
| HikariCP | 5.1 | Пул соединений |
| BCrypt | 0.10.2 | Хеширование паролей |
| Log4j2 | 2.23 | Логирование |
| Bootstrap 5 | 5.3 | CSS фреймворк |

## 📝 Разработка

### Команды Maven

```bash
# Компиляция
mvn compile

# Запуск тестов
mvn test

# Сборка WAR
mvn package

# Очистка
mvn clean

# Установка зависимостей
mvn install
```

### Архитектура

Приложение построено по **слоистой архитектуре**:

```
Controller (Servlets) → Service (Бизнес-логика) → DAO (Доступ к БД)
```

### Паттерны проектирования

- **MVC** - Модель-Вид-Контроллер
- **DAO** - Data Access Object
- **Service Layer** - Слой бизнес-логики
- **Filter** - Фильтры для аутентификации
- **Singleton** - DataSource через DatabaseUtil

## 🔒 Безопасность

- Пароли хешируются с помощью **BCrypt**
- Сессии пользователей защищены
- **CSRF** защита (требуется доработка)
- Ролевая модель доступа (ADMIN/CUSTOMER)
- Фильтры для защиты административных страниц

## 📊 База данных

### Основные сущности:

- **users** - Пользователи
- **books** - Книги
- **cart** - Корзина
- **orders** - Заказы
- **order_items** - Позиции заказа
- **reviews** - Отзывы
- **wishlists** - Списки желаний
- **wishlist_items** - Товары в списках желаний
- **audit_logs** - Логи аудита

## 🐛 Known Issues

- Не реализована загрузка изображений (используются URL)
- Упрощённая проверка права на отзыв
- Нет email уведомлений
- Нет интеграции с платёжной системой

## 📄 Лицензия

Проект создан для учебных целей.

## 👥 Авторы

NLP-Core-Team

## 📞 Поддержка

При возникновении проблем проверьте:
1. Корректность настроек БД в `web.xml`
2. Наличие всех зависимостей в `pom.xml`
3. Версию JDK (требуется JDK 25)
4. Логи в `logs/bookstore.log`
