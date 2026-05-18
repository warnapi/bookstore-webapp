package com.bookstore.controller;

import com.bookstore.service.AuthService;
import com.bookstore.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Контроллер входа пользователей.
 */
@WebServlet("/login")
public class LoginController extends HttpServlet {
    
    private AuthService authService;
    
    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Если пользователь уже авторизован, перенаправляем на главную
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            
            request.setAttribute("error", "Email и пароль обязательны");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }
        
        try {
            // Аутентификация
            User user = authService.authenticate(email.trim(), password);
            
            // Сохраняем пользователя в сессии
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            
            // Установка времени жизни сессии
            if ("on".equals(rememberMe)) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 дней
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30 минут
            }
            
            // Перенаправление на исходную страницу или на главную
            String redirectUrl = request.getContextPath() + "/";
            String intendedUrl = (String) session.getAttribute("intendedUrl");
            if (intendedUrl != null) {
                session.removeAttribute("intendedUrl");
                redirectUrl = intendedUrl;
            }
            
            response.sendRedirect(redirectUrl);
            
        } catch (com.bookstore.service.ServiceException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }
}
