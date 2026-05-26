<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Список желаний");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-heart"></i> Список желаний: <strong>${wishlist.name}</strong></h1>

<div class="row">
    <div class="col-lg-9">
        <c:choose>
            <c:when test="${empty items}">
                <div class="alert alert-info">
                    <p class="mb-0">В этом списке пока нет товаров.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <c:forEach var="item" items="${items}">
                        <div class="col-md-4 mb-4">
                            <div class="card h-100">
                                <c:choose>
                                    <c:when test="${not empty item.bookImageUrl}">
                                        <img src="${item.bookImageUrl}" class="card-img-top" style="height: 200px; object-fit: cover;" alt="${item.bookTitle}">
                                    </c:when>
                                    <c:otherwise>
                                        <div style="height: 200px; background: #f8f9fa; display: flex; align-items: center; justify-content: center;">
                                            <i class="bi bi-book fs-1 text-muted"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="card-body">
                                    <h6 class="card-title">${item.bookTitle}</h6>
                                    <p class="text-muted small">${item.bookAuthor}</p>
                                    <p class="card-text"><strong>${item.bookPrice} BYN</strong></p>
                                    <c:choose>
                                        <c:when test="${item.isInStock}">
                                            <span class="badge bg-success">В наличии</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-danger">Нет в наличии</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="card-footer">
                                    <c:choose>
                                        <c:when test="${item.isInStock}">
                                            <span class="badge bg-success mb-2">В наличии</span>
                                            <form action="${pageContext.request.contextPath}/wishlist" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="addtocart">
                                                <input type="hidden" name="bookId" value="${item.bookId}">
                                                <input type="hidden" name="quantity" value="1">
                                                <button type="submit" class="btn btn-sm btn-primary">
                                                    <i class="bi bi-cart"></i> В корзину
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-danger mb-2">Нет в наличии</span>
                                        </c:otherwise>
                                    </c:choose>
                                    <form action="${pageContext.request.contextPath}/wishlist" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="remove">
                                        <input type="hidden" name="wishlistId" value="${wishlist.id}">
                                        <input type="hidden" name="bookId" value="${item.bookId}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                            <i class="bi bi-trash"></i> Удалить
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    
    <div class="col-lg-3">
        <div class="card">
            <div class="card-body">
                <h5>Действия</h5>
                <hr>
                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-primary w-100 mb-2">
                    <i class="bi bi-search"></i> Добавить книгу
                </a>
                <a href="${pageContext.request.contextPath}/wishlist" class="btn btn-outline-secondary w-100">
                    <i class="bi bi-arrow-left"></i> Все списки
                </a>
            </div>
        </div>
    </div>
</div>
