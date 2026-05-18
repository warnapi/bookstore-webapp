package com.bookstore.controller;

import com.bookstore.service.ReviewService;
import com.bookstore.model.Review;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер отзывов.
 */
@WebServlet("/review/*")
public class ReviewController extends HttpServlet {
    
    private ReviewService reviewService;
    
    @Override
    public void init() throws ServletException {
        reviewService = new ReviewService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User user = (com.bookstore.model.User) session.getAttribute("user");
            
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            
            // Получаем отзывы пользователя
            List<Review> reviews = reviewService.getUserReviews(user.id());
            
            request.setAttribute("reviews", reviews);
            request.getRequestDispatcher("/WEB-INF/views/user/my-reviews.jsp").forward(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка загрузки отзывов", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User user = (com.bookstore.model.User) session.getAttribute("user");
            
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            
            String action = request.getParameter("action");
            
            if ("add".equals(action)) {
                Long bookId = Long.parseLong(request.getParameter("bookId"));
                String bookTitle = request.getParameter("bookTitle");
                int rating = Integer.parseInt(request.getParameter("rating"));
                String text = request.getParameter("text");
                
                reviewService.createReview(
                    user.id(), bookId, bookTitle, user.name(), rating, text
                );
                
                // Перенаправление на страницу книги
                response.sendRedirect(request.getContextPath() + "/book/" + bookId);
                
            } else if ("delete".equals(action)) {
                Long reviewId = Long.parseLong(request.getParameter("reviewId"));
                
                // Проверяем, что отзыв принадлежит пользователю
                reviewService.deleteReview(reviewId, user.id());
                
                response.sendRedirect(request.getContextPath() + "/review");
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки отзыва", e);
        }
    }
}
