package com.bookstore.controller;

import com.bookstore.service.CartService;
import com.bookstore.service.OrderService;
import com.bookstore.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Контроллер оформления заказов.
 */
@WebServlet("/order/*")
public class OrderController extends HttpServlet {
    
    private OrderService orderService;
    private CartService cartService;
    
    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        cartService = new CartService();
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
                // Страница оформления заказа
                boolean hasUnavailableItems = cartService.hasUnavailableItems(user.id());
                request.setAttribute("hasUnavailableItems", hasUnavailableItems);
                
                request.getRequestDispatcher("/WEB-INF/views/order/checkout.jsp").forward(request, response);
                
            } else if (pathInfo.startsWith("/success/")) {
                // Страница успеха
                String orderIdStr = pathInfo.substring(9);
                try {
                    Long orderId = Long.parseLong(orderIdStr);
                    Order order = orderService.findOrderById(orderId).orElse(null);
                    request.setAttribute("order", order);
                } catch (NumberFormatException e) {
                    // Игнорируем
                }
                
                request.getRequestDispatcher("/WEB-INF/views/order/success.jsp").forward(request, response);
                
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки заказа", e);
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
            
            if ("confirm".equals(action)) {
                // Создаём заказ
                Order order = orderService.createOrderFromCart(user.id());
                
                // Перенаправляем на страницу успеха
                String contextPath = request.getContextPath();
                response.sendRedirect(contextPath + "/order/success/" + order.id());
                
            } else if ("cancel".equals(action)) {
                String orderIdStr = request.getParameter("orderId");
                if (orderIdStr != null) {
                    try {
                        Long orderId = Long.parseLong(orderIdStr);
                        orderService.cancelOrder(orderId, user.id());
                    } catch (NumberFormatException e) {
                        // Игнорируем
                    }
                }
                
                response.sendRedirect(request.getContextPath() + "/user/orders");
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки заказа", e);
        }
    }
}
