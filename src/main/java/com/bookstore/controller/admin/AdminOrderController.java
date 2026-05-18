package com.bookstore.controller.admin;

import com.bookstore.service.OrderService;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.OrderStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер управления заказами (администратор).
 */
@WebServlet("/admin/orders")
public class AdminOrderController extends HttpServlet {
    
    private OrderService orderService;
    
    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User admin = (com.bookstore.model.User) session.getAttribute("user");
            
            if (admin == null || admin.role() != com.bookstore.model.Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            String path = request.getParameter("action");
            
            if ("view".equals(path)) {
                Long orderId = Long.parseLong(request.getParameter("id"));
                Order order = orderService.findOrderById(orderId).orElse(null);
                List<OrderItem> items = order != null ? orderService.getOrderItems(orderId) : List.of();
                
                request.setAttribute("order", order);
                request.setAttribute("items", items);
                
                request.getRequestDispatcher("/WEB-INF/views/admin/order-detail.jsp").forward(request, response);
                
            } else {
                // Список заказов
                int page = getIntParameter(request, "page", 1);
                int pageSize = 20;
                
                List<Order> orders = orderService.getAllOrders(page, pageSize);
                long totalOrders = orderService.countOrders();
                int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
                
                request.setAttribute("orders", orders);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", totalPages);
                
                request.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка управления заказами", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User admin = (com.bookstore.model.User) session.getAttribute("user");
            
            if (admin == null || admin.role() != com.bookstore.model.Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            String action = request.getParameter("action");
            
            if ("updateStatus".equals(action)) {
                Long orderId = Long.parseLong(request.getParameter("orderId"));
                String statusStr = request.getParameter("status");
                
                OrderStatus status = OrderStatus.valueOf(statusStr);
                
                orderService.updateOrderStatus(
                    orderId, status, admin.id(), admin.name()
                );
                
                response.sendRedirect(request.getContextPath() + "/admin/orders?action=view&id=" + orderId);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обновления заказа", e);
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
