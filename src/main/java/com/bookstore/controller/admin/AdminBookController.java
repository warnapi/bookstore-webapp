package com.bookstore.controller.admin;

import com.bookstore.service.CatalogService;
import com.bookstore.model.Book;
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
 * Контроллер управления книгами (администратор).
 */
@WebServlet("/admin/books")
public class AdminBookController extends HttpServlet {
    
    private CatalogService catalogService;
    
    @Override
    public void init() throws ServletException {
        catalogService = new CatalogService();
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
            
            String path = request.getParameter("action");
            
            if ("edit".equals(path)) {
                Long bookId = Long.parseLong(request.getParameter("id"));
                Book book = catalogService.findBookById(bookId).orElse(null);
                request.setAttribute("book", book);
                request.getRequestDispatcher("/WEB-INF/views/admin/book-edit.jsp").forward(request, response);
                
            } else if ("add".equals(path)) {
                request.getRequestDispatcher("/WEB-INF/views/admin/book-edit.jsp").forward(request, response);
                
            } else {
                // Список книг
                int page = getIntParameter(request, "page", 1);
                int pageSize = 20;
                
                List<Book> books = catalogService.getAllBooks(page, pageSize);
                long totalBooks = catalogService.countBooks();
                int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
                
                request.setAttribute("books", books);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", totalPages);
                
                request.getRequestDispatcher("/WEB-INF/views/admin/books.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка управления книгами", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession();
            com.bookstore.model.User user = (com.bookstore.model.User) session.getAttribute("user");
            
            if (user == null || user.role() != com.bookstore.model.Role.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            
            String action = request.getParameter("action");
            
            if ("save".equals(action)) {
                String idStr = request.getParameter("id");
                String title = request.getParameter("title");
                String author = request.getParameter("author");
                String isbn = request.getParameter("isbn");
                String priceStr = request.getParameter("price");
                String description = request.getParameter("description");
                String category = request.getParameter("category");
                String stockStr = request.getParameter("stock");
                String imageUrl = request.getParameter("imageUrl");
                
                BigDecimal price = new BigDecimal(priceStr);
                int stock = Integer.parseInt(stockStr);
                
                if (idStr != null && !idStr.isEmpty()) {
                    // Обновление книги
                    Long bookId = Long.parseLong(idStr);
                    Book book = new Book(
                        bookId, title, author, isbn, price, description,
                        category, stock, imageUrl, 0, 0.0, 0
                    );
                    catalogService.updateBook(book);
                } else {
                    // Создание книги
                    Book book = new Book(
                        title, author, isbn, price, description,
                        category, stock, imageUrl
                    );
                    catalogService.createBook(book);
                }
                
                response.sendRedirect(request.getContextPath() + "/admin/books");
                
            } else if ("delete".equals(action)) {
                Long bookId = Long.parseLong(request.getParameter("id"));
                catalogService.deleteBook(bookId);
                
                response.sendRedirect(request.getContextPath() + "/admin/books");
            }
            
        } catch (Exception e) {
            throw new ServletException("Ошибка сохранения книги", e);
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
