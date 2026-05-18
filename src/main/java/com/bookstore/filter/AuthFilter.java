package com.bookstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Фильтр аутентификации.
 * Проверяет наличие аутентифицированного пользователя для защищённых страниц.
 */
@WebFilter(urlPatterns = {"/user/*", "/cart/*", "/order/*", "/wishlist/*", "/review/*"})
public class AuthFilter implements Filter {
    
    private String loginPage;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        loginPage = filterConfig.getInitParameter("loginPage");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // Проверяем, авторизован ли пользователь
        if (session != null && session.getAttribute("user") != null) {
            chain.doFilter(request, response);
            return;
        }
        
        // Перенаправляем на страницу входа
        String contextPath = httpRequest.getContextPath();
        String queryString = httpRequest.getQueryString();
        
        String redirectUrl = contextPath + loginPage;
        if (queryString != null) {
            redirectUrl += "?" + queryString;
        }
        
        httpResponse.sendRedirect(redirectUrl);
    }
    
    @Override
    public void destroy() {
        loginPage = null;
    }
}
