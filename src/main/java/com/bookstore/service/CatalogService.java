package com.bookstore.service;

import com.bookstore.dao.BookDao;
import com.bookstore.model.Book;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления каталогом книг.
 */
public class CatalogService {
    
    private static final Logger logger = LogManager.getLogger(CatalogService.class);
    private final BookDao bookDao;
    
    public CatalogService() {
        this.bookDao = new BookDao();
    }
    
    /**
     * Получает все книги с пагинацией.
     */
    public List<Book> getAllBooks(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return bookDao.findAll(offset, pageSize);
        } catch (SQLException e) {
            logger.error("Ошибка получения книг: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения книг", e);
        }
    }
    
    /**
     * Находит книгу по ID.
     */
    public Optional<Book> findBookById(Long id) throws ServiceException {
        try {
            Optional<Book> book = bookDao.findById(id);
            // Увеличиваем популярность при просмотре
            if (book.isPresent()) {
                bookDao.increasePopularity(id);
            }
            return book;
        } catch (SQLException e) {
            logger.error("Ошибка поиска книги: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка поиска книги", e);
        }
    }
    
    /**
     * Выполняет поиск книг по запросу.
     */
    public List<Book> searchBooks(String query, int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return bookDao.search(query, offset, pageSize);
        } catch (SQLException e) {
            logger.error("Ошибка поиска книг: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка поиска книг", e);
        }
    }
    
    /**
     * Фильтрует книги по категории и цене.
     */
    public List<Book> filterBooks(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                  int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return bookDao.filter(category, minPrice, maxPrice, offset, pageSize);
        } catch (SQLException e) {
            logger.error("Ошибка фильтрации книг: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка фильтрации книг", e);
        }
    }
    
    /**
     * Получает все категории книг.
     */
    public List<String> getAllCategories() throws ServiceException {
        try {
            return bookDao.findAllCategories();
        } catch (SQLException e) {
            logger.error("Ошибка получения категорий: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения категорий", e);
        }
    }
    
    /**
     * Создаёт новую книгу.
     */
    public Book createBook(Book book) throws ServiceException {
        try {
            // Проверка дубликата ISBN
            if (bookDao.existsByIsbn(book.isbn(), null)) {
                throw new ServiceException("Книга с таким ISBN уже существует");
            }
            
            Book createdBook = bookDao.create(book);
            logger.info("Книга создана: {}", createdBook.title());
            return createdBook;
            
        } catch (SQLException e) {
            logger.error("Ошибка создания книги: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка создания книги", e);
        }
    }
    
    /**
     * Обновляет книгу.
     */
    public Book updateBook(Book book) throws ServiceException {
        try {
            // Проверка дубликата ISBN (исключая текущую книгу)
            if (bookDao.existsByIsbn(book.isbn(), book.id())) {
                throw new ServiceException("Книга с таким ISBN уже существует");
            }
            
            bookDao.update(book);
            logger.info("Книга обновлена: {}", book.title());
            return book;
            
        } catch (SQLException e) {
            logger.error("Ошибка обновления книги: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка обновления книги", e);
        }
    }
    
    /**
     * Удаляет книгу.
     */
    public boolean deleteBook(Long id) throws ServiceException {
        try {
            boolean deleted = bookDao.delete(id);
            if (deleted) {
                logger.info("Книга удалена: {}", id);
            }
            return deleted;
            
        } catch (SQLException e) {
            logger.error("Ошибка удаления книги: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка удаления книги", e);
        }
    }
    
    /**
     * Получает общее количество книг.
     */
    public long countBooks() throws ServiceException {
        try {
            return bookDao.count();
        } catch (SQLException e) {
            logger.error("Ошибка подсчёта книг: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка подсчёта книг", e);
        }
    }
    
    /**
     * Получает количество книг по поисковому запросу.
     */
    public long countBySearch(String query) throws ServiceException {
        try {
            return bookDao.countBySearch(query);
        } catch (SQLException e) {
            logger.error("Ошибка подсчёта книг: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка подсчёта книг", e);
        }
    }
    
    /**
     * Получает популярные книги.
     */
    public List<Book> getPopularBooks(int limit) throws ServiceException {
        try {
            return bookDao.findPopular(limit);
        } catch (SQLException e) {
            logger.error("Ошибка получения популярных книг: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка получения популярных книг", e);
        }
    }
    
    /**
     * Обновляет средний рейтинг книги.
     */
    public void updateBookRating(Long bookId, double averageRating, int reviewCount) throws ServiceException {
        try {
            bookDao.updateAverageRating(bookId, averageRating, reviewCount);
        } catch (SQLException e) {
            logger.error("Ошибка обновления рейтинга: {}", e.getMessage(), e);
            throw new ServiceException("Ошибка обновления рейтинга", e);
        }
    }
}
