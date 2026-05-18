<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Главная");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<div class="py-5 text-center">
    <h1><i class="bi bi-book-half"></i> Добро пожаловать в Bookstore</h1>
    <p class="lead">Ваш любимый интернет-магазин книг</p>
</div>

<c:if test="${not empty popularBooks and not empty popularBooks[0]}">
    <h2 class="mb-4">Популярные книги</h2>
    <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-4">
        <c:forEach var="book" items="${popularBooks}">
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
                        <h5 class="card-title text-truncate">${book.title}</h5>
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
                            <p class="price">${book.price} ₽</p>
                            <a href="${pageContext.request.contextPath}/book/${book.id}" class="btn btn-primary w-100">
                                Подробнее
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
    <div class="text-center mt-4">
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-primary btn-lg">
            Смотреть весь каталог
        </a>
    </div>
</c:if>

<c:if test="${empty popularBooks or empty popularBooks[0]}">
    <div class="alert alert-info">
        <p class="mb-0">Каталог книг находится в процессе формирования. Заходите позже!</p>
    </div>
</c:if>
