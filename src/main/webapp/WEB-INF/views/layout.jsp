<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    if (request.getAttribute("title") == null) {
        request.setAttribute("title", "Главная");
    }
    String title = (String) request.getAttribute("title");
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= title != null ? title + " - " : "" %>Интернет-магазин книг</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #2c3e50;
            --secondary-color: #3498db;
            --accent-color: #e74c3c;
        }
        
        body {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        
        .navbar {
            background-color: var(--primary-color) !important;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .navbar-brand {
            font-weight: bold;
            font-size: 1.5rem;
        }
        
        .main-content {
            flex: 1;
            padding: 2rem 0;
        }
        
        .book-card {
            transition: transform 0.2s, box-shadow 0.2s;
            border: none;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .book-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        
        .book-image {
            height: 250px;
            object-fit: cover;
        }
        
        .rating {
            color: #f39c12;
        }
        
        .footer {
            background-color: var(--primary-color);
            color: white;
            padding: 2rem 0;
            margin-top: auto;
        }
        
        .footer a {
            color: #bdc3c7;
            text-decoration: none;
        }
        
        .footer a:hover {
            color: white;
        }
        
        .btn-primary {
            background-color: var(--secondary-color);
            border-color: var(--secondary-color);
        }
        
        .btn-primary:hover {
            background-color: #2980b9;
            border-color: #2980b9;
        }
        
        .price {
            font-size: 1.25rem;
            font-weight: bold;
            color: var(--accent-color);
        }
        
        .alert {
            border-radius: 0.5rem;
        }
    </style>
</head>
<body>
    <!-- Навигация -->
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">
                <i class="bi bi-book-half"></i> Bookstore
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/catalog">
                            <i class="bi bi-grid"></i> Каталог
                        </a>
                    </li>
                    <%-- Адаптация для администратора --%>
                    <c:if test="${not empty sessionScope.user and sessionScope.user.role.name() == 'ADMIN'}">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin/dashboard">
                                <i class="bi bi-speedometer2"></i> Админ-панель
                            </a>
                        </li>
                    </c:if>
                </ul>
                <ul class="navbar-nav">
                    <c:choose>
                        <c:when test="${not empty sessionScope.user}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/user/">
                                    <i class="bi bi-person"></i> ${sessionScope.user.name}
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/wishlist/">
                                    <i class="bi bi-heart"></i> Списки
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/cart">
                                    <i class="bi bi-cart"></i> Корзина
                                </a>
                            </li>
                            <li class="nav-item">
                                <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline">
                                    <button type="submit" class="nav-link btn btn-link">
                                        <i class="bi bi-box-arrow-right"></i> Выход
                                    </button>
                                </form>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/login">
                                    <i class="bi bi-box-arrow-in-right"></i> Вход
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/register">
                                    <i class="bi bi-person-plus"></i> Регистрация
                                </a>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Основной контент -->
    <main class="main-content">
        <div class="container">
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            
            <c:out value="<%-- Динамическое содержимое страницы --%>" />
        </div>
    </main>

    <!-- Футер -->
    <footer class="footer">
        <div class="container">
            <div class="row">
                <div class="col-md-4">
                    <h5>Bookstore</h5>
                    <p>Ваш надёжный интернет-магазин книг.</p>
                </div>
                <div class="col-md-4">
                    <h5>Навигация</h5>
                    <ul class="list-unstyled">
                        <li><a href="${pageContext.request.contextPath}/catalog">Каталог</a></li>
                        <li><a href="${pageContext.request.contextPath}/about">О нас</a></li>
                        <li><a href="${pageContext.request.contextPath}/contact">Контакты</a></li>
                    </ul>
                </div>
                <div class="col-md-4">
                    <h5>Контакты</h5>
                    <p><i class="bi bi-geo-alt"></i> Минск, Беларусь</p>
                    <p><i class="bi bi-envelope"></i> info@bookstore.by</p>
                    <p><i class="bi bi-telephone"></i> +375 (17) 123-45-67</p>
                </div>
            </div>
            <hr>
            <div class="text-center">
                <p>&copy; 2025 Bookstore. Все права защищены.</p>
            </div>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
