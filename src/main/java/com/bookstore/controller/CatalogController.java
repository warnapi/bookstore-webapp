package com.bookstore.controller;

import com.bookstore.service.CatalogService;
import com.bookstore.model.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Контроллер каталога книг.
 */
@WebServlet("/catalog")
public class CatalogController extends HttpServlet {
    
    private CatalogService catalogService;
    
    @Override
    public void init() throws ServletException {
        catalogService = new CatalogService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Параметры страницы
            int page = getIntParameter(request, "page", 1);
            int pageSize = 12;
            
            // Поиск
            String search = request.getParameter("search");
            
            // Фильтры
            String category = request.getParameter("category");
            String minPrice = request.getParameter("minPrice");
            String maxPrice = request.getParameter("maxPrice");
            
            List<Book> books;
            long totalBooks;
            
            if (search != null && !search.trim().isEmpty()) {
                // Поиск
                books = catalogService.searchBooks(search, page, pageSize);
                totalBooks = catalogService.countBySearch(search);
                request.setAttribute("searchQuery", search.trim());
            } else {
                // Фильтрация
                BigDecimal minPriceVal = parseBigDecimal(minPrice);
                BigDecimal maxPriceVal = parseBigDecimal(maxPrice);
                
                books = catalogService.filterBooks(category, minPriceVal, maxPriceVal, page, pageSize);
                totalBooks = catalogService.countBooks();
                
                request.setAttribute("category", category);
                request.setAttribute("minPrice", minPrice);
                request.setAttribute("maxPrice", maxPrice);
            }
            
            // Расчёт пагинации
            int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
            
            request.setAttribute("books", books);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalBooks", totalBooks);
            request.setAttribute("categories", catalogService.getAllCategories());
            
            request.getRequestDispatcher("/WEB-INF/views/catalog/catalog.jsp").forward(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка загрузки каталога", e);
        }
    }
    
    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        try {
            int value = Integer.parseInt(request.getParameter(name));
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
