<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Каталог");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<div class="row">
    <!-- Фильтры -->
    <div class="col-md-3">
        <div class="card mb-4">
            <div class="card-header">
                <h5 class="mb-0"><i class="bi bi-funnel"></i> Фильтры</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/catalog" method="get">
                    <c:if test="${not empty searchQuery}">
                        <input type="hidden" name="search" value="${searchQuery}">
                    </c:if>
                    
                    <div class="mb-3">
                        <label class="form-label">Категория</label>
                        <select name="category" class="form-select">
                            <option value="">Все категории</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat}" ${cat == category ? 'selected' : ''}>${cat}</option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label class="form-label">Цена от</label>
                        <input type="number" name="minPrice" class="form-control" 
                               value="${minPrice}" placeholder="0">
                    </div>
                    
                    <div class="mb-3">
                        <label class="form-label">Цена до</label>
                        <input type="number" name="maxPrice" class="form-control" 
                               value="${maxPrice}" placeholder="10000">
                    </div>
                    
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="bi bi-search"></i> Применить
                    </button>
                </form>
            </div>
        </div>
    </div>
    
    <!-- Список книг -->
    <div class="col-md-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3>
                <c:choose>
                    <c:when test="${not empty searchQuery}">
                        Поиск: "${searchQuery}" (${totalBooks} книг)
                    </c:when>
                    <c:otherwise>
                        Каталог книг (${totalBooks})
                    </c:otherwise>
                </c:choose>
            </h3>
            
            <form action="${pageContext.request.contextPath}/catalog" method="get" class="d-flex">
                <input type="text" name="search" class="form-control me-2" 
                       placeholder="Поиск книг..." value="${searchQuery}">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-search"></i>
                </button>
            </form>
        </div>
        
        <c:choose>
            <c:when test="${not empty books and books[0] != null}">
                <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
                    <c:forEach var="book" items="${books}">
                        <div class="col">
                            <div class="card h-100 book-card">
                                <c:choose>
                                    <c:when test="${not empty book.imageUrl}">
                                        <img src="${book.imageUrl}" class="card-img-top book-image" alt="${book.title}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="book-image bg-light d-flex align-items-center justify-content-center">
                                            <i class="bi bi-book fs-1 text-muted"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="card-body d-flex flex-column">
                                    <h5 class="card-title">${book.title}</h5>
                                    <p class="card-text text-muted">${book.author}</p>
                                    <c:if test="${book.averageRating > 0}">
                                        <div class="rating mb-2">
                                            <c:forEach var="i" begin="1" end="5">
                                                <i class="bi ${i <= book.averageRating ? 'bi-star-fill' : 'bi-star'}"></i>
                                            </c:forEach>
                                            <small class="text-muted">(${book.averageRating})</small>
                                        </div>
                                    </c:if>
                                    <div class="mt-auto">
                                        <p class="price">${book.price} BYN</p>
                                        <a href="${pageContext.request.contextPath}/book/${book.id}" class="btn btn-outline-primary w-100">
                                            Подробнее
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                
                <!-- Пагинация -->
                <c:if test="${totalPages > 1}">
                    <nav class="mt-4">
                        <ul class="pagination justify-content-center">
                            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                <a class="page-link" href="?page=${currentPage - 1}${not empty searchQuery ? '&search=' + searchQuery : ''}">
                                    <i class="bi bi-chevron-left"></i>
                                </a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${i == currentPage ? 'active' : ''}">
                                    <a class="page-link" href="?page=${i}${not empty searchQuery ? '&search=' + searchQuery : ''}">
                                        ${i}
                                    </a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="?page=${currentPage + 1}${not empty searchQuery ? '&search=' + searchQuery : ''}">
                                    <i class="bi bi-chevron-right"></i>
                                </a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="alert alert-info">
                    <p class="mb-0">Книги не найдены.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
