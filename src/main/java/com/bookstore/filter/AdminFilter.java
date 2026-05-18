package com.bookstore.filter;

import com.bookstore.model.Role;
import com.bookstore.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Фильтр проверки прав администратора.
 */
@WebFilter("/admin/*")
public class AdminFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // Проверяем, авторизован ли пользователь
        if (session == null || session.getAttribute("user") == null) {
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Проверяем, является ли пользователь администратором
        if (user.role() != Role.ADMIN) {
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
