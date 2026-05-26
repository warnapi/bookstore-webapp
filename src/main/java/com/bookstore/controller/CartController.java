package com.bookstore.controller;

import com.bookstore.service.CartService;
import com.bookstore.model.CartItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер корзины покупок.
 */
@WebServlet("/cart/*")
public class CartController extends HttpServlet {
    
    private CartService cartService;
    
    @Override
    public void init() throws ServletException {
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
            
            // Получаем товары корзины
            List<CartItem> cartItems = cartService.getCartItems(user.id());
            
            // Вычисляем общую сумму
            java.math.BigDecimal total = cartService.getCartTotal(user.id());
            
            request.setAttribute("cartItems", cartItems);
            request.setAttribute("cartTotal", total);
            request.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка загрузки корзины", e);
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
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                
                cartService.addToCart(user.id(), bookId, quantity);
                
            } else if ("update".equals(action)) {
                Long bookId = Long.parseLong(request.getParameter("bookId"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                
                cartService.updateQuantity(user.id(), bookId, quantity);
                
            } else if ("remove".equals(action)) {
                Long bookId = Long.parseLong(request.getParameter("bookId"));
                
                cartService.removeFromCart(user.id(), bookId);
            }
            
            // Перенаправление обратно в корзину
            response.sendRedirect(request.getContextPath() + "/cart");
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки корзины", e);
        }
    }
}
