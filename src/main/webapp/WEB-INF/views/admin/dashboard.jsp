<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Панель администратора");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-speedometer2"></i> Панель администратора</h1>

<!-- Статистика -->
<div class="row mb-4">
    <div class="col-md-4">
        <div class="card text-white bg-primary">
            <div class="card-body">
                <h5>Общие продажи</h5>
                <h2>${totalSales} ₽</h2>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card text-white bg-success">
            <div class="card-body">
                <h5>Всего заказов</h5>
                <h2>${totalOrders}</h2>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card text-white bg-info">
            <div class="card-body">
                <h5>Книг в каталоге</h5>
                <h2>${bookStatistics.totalBooks()}</h2>
            </div>
        </div>
    </div>
</div>

<!-- Быстрые ссылки -->
<div class="row mb-4">
    <div class="col-md-3">
        <a href="${pageContext.request.contextPath}/admin/books" class="btn btn-primary w-100 py-3">
            <i class="bi bi-book"></i> Управление книгами
        </a>
    </div>
    <div class="col-md-3">
        <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-success w-100 py-3">
            <i class="bi bi-receipt"></i> Заказы
        </a>
    </div>
    <div class="col-md-3">
        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-info w-100 py-3">
            <i class="bi bi-people"></i> Пользователи
        </a>
    </div>
    <div class="col-md-3">
        <a href="${pageContext.request.contextPath}/admin/reviews?action=pending" class="btn btn-warning w-100 py-3">
            <i class="bi bi-chat"></i> Отзывы на модерации
        </a>
    </div>
</div>

<!-- Последние заказы -->
<div class="card">
    <div class="card-header">
        <h5 class="mb-0">Последние заказы</h5>
    </div>
    <div class="card-body">
        <c:choose>
            <c:when test="${not empty recentOrders and recentOrders[0] != null}">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>Номер</th>
                            <th>Пользователь</th>
                            <th>Сумма</th>
                            <th>Статус</th>
                            <th>Дата</th>
                            <th>Действия</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="order" items="${recentOrders}">
                            <tr>
                                <td>${order.orderNumber}</td>
                                <td>${order.userId}</td>
                                <td>${order.totalAmount} ₽</td>
                                <td>
                                    <span class="badge bg-${order.status.name() == 'PAID' || order.status.name() == 'DELIVERED' ? 'success' : 
                                                        order.status.name() == 'PENDING' ? 'warning' : 
                                                        order.status.name() == 'CANCELLED' ? 'danger' : 'secondary'}">
                                        ${order.status.name()}
                                    </span>
                                </td>
                                <td>${order.createdAt}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/admin/orders?action=view&id=${order.id}" class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-eye"></i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p class="text-muted">Заказов пока нет</p>
            </c:otherwise>
        </c:choose>
    </div>
</div>
