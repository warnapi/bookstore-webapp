package com.bookstore.controller;

import com.bookstore.service.WishlistService;
import com.bookstore.service.CartService;
import com.bookstore.model.Wishlist;
import com.bookstore.model.WishlistItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер списков желаний.
 */
@WebServlet("/wishlist/*")
public class WishlistController extends HttpServlet {
    
    private WishlistService wishlistService;
    private CartService cartService;
    
    @Override
    public void init() throws ServletException {
        wishlistService = new WishlistService();
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
                // Список всех списков желаний
                List<Wishlist> wishlists = wishlistService.getUserWishlists(user.id());
                request.setAttribute("wishlists", wishlists);
                
                request.getRequestDispatcher("/WEB-INF/views/wishlist/wishlists.jsp").forward(request, response);
                
            } else if (pathInfo.startsWith("/view/")) {
                // Просмотр конкретного списка желаний
                String wishlistIdStr = pathInfo.substring(6);
                try {
                    Long wishlistId = Long.parseLong(wishlistIdStr);
                    
                    Wishlist wishlist = wishlistService.findWishlistById(wishlistId).orElse(null);
                    if (wishlist == null || !wishlist.userId().equals(user.id())) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    
                    List<WishlistItem> items = wishlistService.getWishlistItems(wishlistId);
                    
                    request.setAttribute("wishlist", wishlist);
                    request.setAttribute("items", items);
                    
                    request.getRequestDispatcher("/WEB-INF/views/wishlist/wishlist-items.jsp").forward(request, response);
                    
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки списка желаний", e);
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
            
            if ("create".equals(action)) {
                String name = request.getParameter("name");
                if (name != null && !name.trim().isEmpty()) {
                    wishlistService.createWishlist(user.id(), name.trim());
                }
                
            } else if ("delete".equals(action)) {
                Long wishlistId = Long.parseLong(request.getParameter("wishlistId"));
                wishlistService.deleteWishlist(wishlistId);
                
            } else if ("add".equals(action)) {
                Long wishlistId = Long.parseLong(request.getParameter("wishlistId"));
                Long bookId = Long.parseLong(request.getParameter("bookId"));
                
                try {
                    wishlistService.addItemToWishlist(wishlistId, bookId);
                } catch (com.bookstore.service.ServiceException e) {
                    // Книга уже в списке - игнорируем
                }
                
            } else if ("remove".equals(action)) {
                Long wishlistId = Long.parseLong(request.getParameter("wishlistId"));
                Long bookId = Long.parseLong(request.getParameter("bookId"));
                
                wishlistService.removeItemFromWishlist(wishlistId, bookId);
                
            } else if ("addtocart".equals(action)) {
                Long bookId = Long.parseLong(request.getParameter("bookId"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                
                cartService.addToCart(user.id(), bookId, quantity);
            }
            
            // Перенаправление
            String redirectUrl = request.getContextPath() + "/wishlist";
            String wishlistId = request.getParameter("wishlistId");
            if (wishlistId != null) {
                redirectUrl += "/view/" + wishlistId;
            }
            
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            throw new ServletException("Ошибка обработки списка желаний", e);
        }
    }
}
