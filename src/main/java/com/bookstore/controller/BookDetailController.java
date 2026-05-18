package com.bookstore.controller;

import com.bookstore.service.CatalogService;
import com.bookstore.service.ReviewService;
import com.bookstore.model.Book;
import com.bookstore.model.Review;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер страницы товара.
 */
@WebServlet("/book/*")
public class BookDetailController extends HttpServlet {
    
    private CatalogService catalogService;
    private ReviewService reviewService;
    
    @Override
    public void init() throws ServletException {
        catalogService = new CatalogService();
        reviewService = new ReviewService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Получаем ID книги из URL
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            String bookIdStr = pathInfo.substring(1); // Убираем ведущий '/'
            Long bookId;
            
            try {
                bookId = Long.parseLong(bookIdStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Получаем книгу
            Optional<Book> bookOpt = catalogService.findBookById(bookId);
            if (bookOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Book book = bookOpt.get();
            
            // Получаем отзывы
            List<Review> reviews = reviewService.getBookReviews(bookId);
            
            // Проверяем, может ли текущий пользователь оставить отзыв
            boolean canReview = false;
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                Long userId = ((com.bookstore.model.User) session.getAttribute("user")).id();
                canReview = reviewService.canUserReviewBook(userId, bookId);
            }
            
            request.setAttribute("book", book);
            request.setAttribute("reviews", reviews);
            request.setAttribute("canReview", canReview);
            
            request.getRequestDispatcher("/WEB-INF/views/catalog/book-detail.jsp").forward(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка загрузки страницы товара", e);
        }
    }
}
