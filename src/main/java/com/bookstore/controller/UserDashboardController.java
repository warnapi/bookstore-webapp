package com.bookstore.controller;

import com.bookstore.service.OrderService;
import com.bookstore.service.AuthService;
import com.bookstore.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер личного кабинета пользователя.
 */
@WebServlet("/user/*")
public class UserDashboardController extends HttpServlet {
    
    private OrderService orderService;
    private AuthService authService;
    
    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        authService = new AuthService();
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
            
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Главная страница кабинета
                request.getRequestDispatcher("/WEB-INF/views/user/dashboard.jsp").forward(request, response);
                
            } else if (pathInfo.startsWith("/orders")) {
                // История заказов
                int page = getIntParameter(request, "page", 1);
                int pageSize = 10;
                
                List<Order> orders = orderService.getUserOrders(user.id(), page, pageSize);
                long totalOrders = orderService.countOrders();
                int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
                
                request.setAttribute("orders", orders);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", totalPages);
                
                request.getRequestDispatcher("/WEB-INF/views/user/orders.jsp").forward(request, response);
                
            } else if (pathInfo.startsWith("/profile")) {
                // Профиль пользователя
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/user/profile.jsp").forward(request, response);
                
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки запроса пользователя", e);
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
            
            if ("updateProfile".equals(action)) {
                String name = request.getParameter("name");
                String email = request.getParameter("email");
                
                if (name != null && email != null) {
                    authService.updateUser(user.id(), email.trim(), name.trim());
                    
                    // Обновляем пользователя в сессии
                    com.bookstore.model.User updatedUser = authService.findUserById(user.id()).orElse(user);
                    session.setAttribute("user", updatedUser);
                    
                    request.setAttribute("success", "Профиль обновлён");
                }
                
                response.sendRedirect(request.getContextPath() + "/user/profile");
                
            } else if ("changePassword".equals(action)) {
                String oldPassword = request.getParameter("oldPassword");
                String newPassword = request.getParameter("newPassword");
                String confirmPassword = request.getParameter("confirmPassword");
                
                if (!newPassword.equals(confirmPassword)) {
                    request.setAttribute("error", "Новые пароли не совпадают");
                    response.sendRedirect(request.getContextPath() + "/user/profile");
                    return;
                }
                
                boolean success = authService.changePassword(user.id(), oldPassword, newPassword);
                
                if (success) {
                    request.setAttribute("success", "Пароль изменён");
                } else {
                    request.setAttribute("error", "Ошибка изменения пароля");
                }
                
                response.sendRedirect(request.getContextPath() + "/user/profile");
                
            } else if ("cancelOrder".equals(action)) {
                Long orderId = Long.parseLong(request.getParameter("orderId"));
                orderService.cancelOrder(orderId, user.id());
                
                response.sendRedirect(request.getContextPath() + "/user/orders");
                
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки запроса пользователя", e);
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
