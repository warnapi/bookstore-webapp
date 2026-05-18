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
 * Контроллер регистрации пользователей.
 */
@WebServlet("/register")
public class RegisterController extends HttpServlet {
    
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
        
        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String name = request.getParameter("name");
        
        // Валидация
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            name == null || name.trim().isEmpty()) {
            
            request.setAttribute("error", "Все поля обязательны для заполнения");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }
        
        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Пароли не совпадают");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }
        
        try {
            // Регистрация пользователя
            User user = authService.register(email.trim(), password, name.trim());
            
            // Автоматический вход после регистрации
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            
            // Перенаправление на главную
            response.sendRedirect(request.getContextPath() + "/");
            
        } catch (com.bookstore.service.ServiceException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
        }
    }
}
