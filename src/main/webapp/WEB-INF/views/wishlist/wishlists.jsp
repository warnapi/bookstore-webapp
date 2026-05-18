<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Списки желаний");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-heart"></i> Мои списки желаний</h1>

<div class="row">
    <div class="col-md-4">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Создать новый список</h5>
                <form action="${pageContext.request.contextPath}/wishlist" method="post">
                    <input type="hidden" name="action" value="create">
                    <div class="mb-3">
                        <input type="text" class="form-control" name="name" placeholder="Название списка" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="bi bi-plus"></i> Создать
                    </button>
                </form>
            </div>
        </div>
    </div>
    
    <div class="col-md-8">
        <c:choose>
            <c:when test="${empty wishlists}">
                <div class="alert alert-info">
                    <p class="mb-0">У вас пока нет списков желаний.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <c:forEach var="wishlist" items="${wishlists}">
                        <div class="col-md-6 mb-4">
                            <div class="card h-100">
                                <div class="card-body">
                                    <h5 class="card-title">${wishlist.name}</h5>
                                    <p class="text-muted mb-3">Товаров: ${wishlist.itemCount}</p>
                                    <a href="${pageContext.request.contextPath}/wishlist/view/${wishlist.id}" class="btn btn-outline-primary">
                                        <i class="bi bi-eye"></i> Просмотр
                                    </a>
                                    <form action="${pageContext.request.contextPath}/wishlist" method="post" class="mt-2">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="wishlistId" value="${wishlist.id}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger" 
                                                onclick="return confirm('Удалить список?')">
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
</div>
