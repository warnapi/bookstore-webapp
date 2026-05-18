package com.bookstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Фильтр установки кодировки символов.
 */
@WebFilter("/*")
public class CharacterEncodingFilter implements Filter {
    
    private String encoding;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        encoding = filterConfig.getInitParameter("encoding");
        if (encoding == null || encoding.isEmpty()) {
            encoding = "UTF-8";
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html; charset=" + encoding);
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        encoding = null;
    }
}
