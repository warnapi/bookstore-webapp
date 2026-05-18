package com.bookstore.controller;

import com.bookstore.service.CatalogService;
import com.bookstore.model.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер главной страницы.
 */
@WebServlet("/")
public class HomeController extends HttpServlet {
    
    private CatalogService catalogService;
    
    @Override
    public void init() throws ServletException {
        catalogService = new CatalogService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Получаем популярные книги
            List<Book> popularBooks = catalogService.getPopularBooks(8);
            
            request.setAttribute("popularBooks", popularBooks);
            request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка загрузки главной страницы", e);
        }
    }
}
