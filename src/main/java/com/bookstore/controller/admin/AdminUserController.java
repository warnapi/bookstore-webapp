package com.bookstore.controller.admin;

import com.bookstore.service.AuthService;
import com.bookstore.dao.UserDao;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер управления пользователями (администратор).
 */
@WebServlet("/admin/users")
public class AdminUserController extends HttpServlet {
    
    private AuthService authService;
    private UserDao userDao;
    
    @Override
    public void init() throws ServletException {
        authService = new AuthService();
        userDao = new UserDao();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User admin = (com.bookstore.model.User) session.getAttribute("user");
            
            if (admin == null || admin.role() != Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            int page = getIntParameter(request, "page", 1);
            int pageSize = 20;
            int offset = (page - 1) * pageSize;
            
            List<User> users = userDao.findAll(offset, pageSize);
            long totalUsers = userDao.count();
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
            
            request.setAttribute("users", users);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка управления пользователями", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User admin = (com.bookstore.model.User) session.getAttribute("user");
            
            if (admin == null || admin.role() != Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            String action = request.getParameter("action");
            
            if ("updateRole".equals(action)) {
                Long userId = Long.parseLong(request.getParameter("userId"));
                // TODO: реализовать обновление роли пользователя
                // String roleStr = request.getParameter("role");
                // Role role = "ADMIN".equals(roleStr) ? Role.ADMIN : Role.CUSTOMER;
                // authService.updateUser(userId, user.email(), user.name(), role);
                
                response.sendRedirect(request.getContextPath() + "/admin/users");
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обновления пользователя", e);
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
