package com.bookstore.controller.admin;

import com.bookstore.service.AdminService;
import com.bookstore.service.CatalogService;
import com.bookstore.service.OrderService;
import com.bookstore.model.Book;
import com.bookstore.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Контроллер административной панели (главная).
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardController extends HttpServlet {
    
    private AdminService adminService;
    private CatalogService catalogService;
    private OrderService orderService;
    
    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
        catalogService = new CatalogService();
        orderService = new OrderService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User user = (com.bookstore.model.User) session.getAttribute("user");
            
            if (user == null || user.role() != com.bookstore.model.Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            // Статистика
            adminService.getBookStatistics();
            adminService.getUserStatistics();
            BigDecimal totalSales = orderService.getTotalSales();
            long totalOrders = orderService.countOrders();
            
            // Последние заказы
            List<Order> recentOrders = orderService.getAllOrders(1, 10);
            
            // Популярные книги
            List<Book> popularBooks = catalogService.getPopularBooks(5);
            
            request.setAttribute("totalSales", totalSales);
            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("recentOrders", recentOrders);
            request.setAttribute("popularBooks", popularBooks);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка загрузки панели администратора", e);
        }
    }
}
