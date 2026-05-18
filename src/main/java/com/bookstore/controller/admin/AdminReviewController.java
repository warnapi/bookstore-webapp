package com.bookstore.controller.admin;

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
 * Контроллер управления отзывами (администратор).
 */
@WebServlet("/admin/reviews")
public class AdminReviewController extends HttpServlet {
    
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
            com.bookstore.model.User admin = (com.bookstore.model.User) session.getAttribute("user");
            
            if (admin == null || admin.role() != com.bookstore.model.Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            String path = request.getParameter("action");
            
            if ("pending".equals(path)) {
                // Отзывы на модерации
                List<Review> reviews = reviewService.getPendingReviews();
                request.setAttribute("reviews", reviews);
                request.setAttribute("mode", "pending");
                
                request.getRequestDispatcher("/WEB-INF/views/admin/reviews.jsp").forward(request, response);
                
            } else {
                // Все отзывы
                int page = getIntParameter(request, "page", 1);
                
                List<Review> reviews = reviewService.getPendingReviews();
                
                request.setAttribute("reviews", reviews);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", 1);
                request.setAttribute("mode", "all");
                
                request.getRequestDispatcher("/WEB-INF/views/admin/reviews.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка управления отзывами", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User admin = (com.bookstore.model.User) session.getAttribute("user");
            
            if (admin == null || admin.role() != com.bookstore.model.Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            String action = request.getParameter("action");
            
            if ("approve".equals(action)) {
                Long reviewId = Long.parseLong(request.getParameter("reviewId"));
                reviewService.approveReview(reviewId, admin.id(), admin.name());
                
            } else if ("hide".equals(action)) {
                Long reviewId = Long.parseLong(request.getParameter("reviewId"));
                reviewService.hideReview(reviewId, admin.id(), admin.name());
                
            } else if ("delete".equals(action)) {
                Long reviewId = Long.parseLong(request.getParameter("reviewId"));
                reviewService.deleteReview(reviewId, admin.id(), admin.name());
            }
            
            String redirectUrl = request.getContextPath() + "/admin/reviews";
            String mode = request.getParameter("mode");
            if ("pending".equals(mode)) {
                redirectUrl += "?action=pending";
            }
            
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка управления отзывами", e);
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
}
